package com.paduvi.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.paduvi.app.Main.Solution;
import com.paduvi.app.models.Contractor;
import com.paduvi.app.models.Pack;
import com.paduvi.app.models.Product;
import com.paduvi.app.models.Project;
import com.paduvi.app.models.Timestamp;
import com.paduvi.app.models.TimestampDeserializer;

public class App {
	public static void createNoCell(Row row, AtomicInteger colNum, int value) {
		Cell cell = row.createCell(colNum.getAndIncrement());
		cell.setCellValue(value);
	}

	public static void createBuyerProfitCell(Row row, AtomicInteger colNum, Project project, List<Solution> solutions) {
		Cell cell = row.createCell(colNum.getAndIncrement());

		double profit = IntStream.range(0, project.getPackages().size()).parallel().mapToDouble(i -> {
			Contractor contractor = solutions.get(i).getContractor();
			Timestamp executedDate = solutions.get(i).getExecutedDate();
			Pack pkg = project.getPackages().get(i);
			double sumSell = pkg.getProducts().parallelStream().mapToDouble(p -> {
				int productId = p.getProductId();
				Product temp = contractor.getProducts().parallelStream().filter(p1 -> p1.getProductId() == productId)
						.findAny().get();
				return pkg.getEstimatedCost()
						- temp.getSellPrice() * (1 - temp.getDiscountRate(executedDate)) * p.getQuantity();
			}).sum();
			return sumSell
					* Math.exp(project.getInflationRate() * (executedDate.get() - project.getStartDate().get()) / 7);
		}).sum();
		cell.setCellValue(String.format("%,.2f", profit));
	}

	public static void createBuyerProfitDiffCell(Row row, AtomicInteger colNum, Project project,
			List<Solution> solutions, List<Contractor> contractors) {
		Cell cell = row.createCell(colNum.getAndIncrement());
		double[] listRevenues = new double[contractors.size()];
		IntStream.range(0, project.getPackages().size()).parallel().forEach(i -> {
			Contractor contractor = solutions.get(i).getContractor();
			Timestamp executedDate = solutions.get(i).getExecutedDate();
			Pack pkg = project.getPackages().get(i);
			double differentPrice = pkg.getProducts().parallelStream().mapToDouble(p -> {
				int productId = p.getProductId();
				Product temp = contractor.getProducts().parallelStream().filter(p1 -> p1.getProductId() == productId)
						.findAny().get();
				return (temp.getSellPrice() * (1 - temp.getDiscountRate(executedDate)) - temp.getBuyPrice())
						* p.getQuantity();
			}).sum();
			listRevenues[contractor.getContractorId()] += differentPrice
					* Math.exp(project.getInflationRate() * (executedDate.get() - project.getStartDate().get()) / 7);
		});

		double sum = 0;
		for (int i = 0; i < contractors.size() - 1; i++) {
			for (int j = i; j < contractors.size(); j++) {
				sum += Math.abs(contractors.get(i).getRelationship() * listRevenues[i]
						- contractors.get(j).getRelationship() * listRevenues[j]);
			}
		}

		cell.setCellValue(String.format("%,.2f", sum));
	}

	public static void createQualityProfitCell(Row row, AtomicInteger colNum, List<Solution> solutions) {
		Cell cell = row.createCell(colNum.getAndIncrement());
		double sumQuality = solutions.parallelStream().mapToDouble(solution -> solution.getContractor().getQuality())
				.sum();
		cell.setCellValue(sumQuality);
	}

	public static void createPackageProfitCell(Row row, AtomicInteger colNum, Solution solution, Pack pkg,
			Project project) {
		Cell cell = row.createCell(colNum.getAndIncrement());

		Contractor contractor = solution.getContractor();
		Timestamp executedDate = solution.getExecutedDate();
		double differentPrice = pkg.getProducts().parallelStream().mapToDouble(p -> {
			int productId = p.getProductId();
			Product temp = contractor.getProducts().parallelStream().filter(p1 -> p1.getProductId() == productId)
					.findAny().get();
			return (temp.getSellPrice() * (1 - temp.getDiscountRate(executedDate)) - temp.getBuyPrice())
					* p.getQuantity();
		}).sum();
		cell.setCellValue(String.format("%,.2f", differentPrice
				* Math.exp(project.getInflationRate() * (executedDate.get() - project.getStartDate().get()) / 7)));
	}

	public static void main(String[] args) throws Exception {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
		gsonBuilder.registerTypeAdapter(Timestamp.class, new TimestampDeserializer());
		Gson gson = gsonBuilder.create();
		File file = new File(App.class.getResource("/data_sample/data.json").getPath());

		JsonObject json = gson.fromJson(new FileReader(file), JsonObject.class);
		Project project = gson.fromJson(json, Project.class);

		List<Contractor> contractors = gson.fromJson(json.get("contractors").toString(),
				new TypeToken<List<Contractor>>() {
				}.getType());

		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("NSGA-II Report");
		List<String> headers = new ArrayList<String>();
		Collections.addAll(headers, "STT", "Buyer Profit", "Provider Profit Diff", "Quality");
		for (int i = 0; i < project.getPackages().size(); i++) {
			headers.add("Package " + i + " Provider");
			headers.add("Package " + i + " Date");
			headers.add("Package " + i + " Profit");
		}

		int rowNum = 0;
		System.out.println("Creating excel");
		Row row = sheet.createRow(rowNum++);
		int colNum = 0;
		for (String header : headers) {
			Cell cell = row.createCell(colNum++);
			cell.setCellValue(header);
		}

		sheet.createFreezePane(0, 1);

		for (int i = 0; i < 40; i++) {
			Main app = new Main(project, contractors);
			List<Solution> solutions = app.getSolution();
			app.printResult();
			row = sheet.createRow(rowNum++);

			AtomicInteger colNo = new AtomicInteger(0);
			createNoCell(row, colNo, i + 1);
			createBuyerProfitCell(row, colNo, project, solutions);
			createBuyerProfitDiffCell(row, colNo, project, solutions, contractors);
			createQualityProfitCell(row, colNo, solutions);

			for (int j = 0; j < solutions.size(); j++) {
				Cell cell = row.createCell(colNo.getAndIncrement());
				cell.setCellValue(solutions.get(j).getContractor().getContractorId());
				cell = row.createCell(colNo.getAndIncrement());
				cell.setCellValue(solutions.get(j).getExecutedDate().toString());
				createPackageProfitCell(row, colNo, solutions.get(j), project.getPackages().get(j), project);
			}
		}

		row = sheet.getRow(0);
		Iterator<Cell> cellIterator = row.cellIterator();
		while (cellIterator.hasNext()) {
			Cell cell = cellIterator.next();
			int columnIndex = cell.getColumnIndex();
			sheet.autoSizeColumn(columnIndex);
		}

		try {
			FileOutputStream outputStream = new FileOutputStream("report.xlsx");
			workbook.write(outputStream);
			workbook.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Done");

	}
}
