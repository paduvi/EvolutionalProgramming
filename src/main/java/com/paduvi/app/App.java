package com.paduvi.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.paduvi.TestApp;
import com.paduvi.app.Main.Solution;
import com.paduvi.app.models.Contractor;
import com.paduvi.app.models.Project;
import com.paduvi.app.models.Timestamp;
import com.paduvi.app.models.TimestampDeserializer;

public class App {
	public static void main(String[] args) throws Exception {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
		gsonBuilder.registerTypeAdapter(Timestamp.class, new TimestampDeserializer());
		Gson gson = gsonBuilder.create();
		File file = new File(TestApp.class.getResource("/data_sample/data.json").getPath());

		JsonObject json = gson.fromJson(new FileReader(file), JsonObject.class);
		Project project = gson.fromJson(json, Project.class);

		List<Contractor> contractors = gson.fromJson(json.get("contractors").toString(),
				new TypeToken<List<Contractor>>() {
				}.getType());

		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("NSGA-II Report");
		String[] headers = new String[project.getPackages().size() * 2 + 1];
		headers[0] = "STT";
		for (int i = 0; i < project.getPackages().size(); i++) {
			headers[1 + 2 * i] = "Package " + i + " Provider";
			headers[2 * (i + 1)] = "Package " + i + " Date";
		}

		int rowNum = 0;
		System.out.println("Creating excel");
		Row row = sheet.createRow(rowNum++);
		int colNum = 0;
		for (String header : headers) {
			Cell cell = row.createCell(colNum++);
			cell.setCellValue(header);
		}

		for (int i = 0; i < 10; i++) {
			Main app = new Main(project, contractors);
			List<Solution> solutions = app.getSolution();
			app.printResult();
			row = sheet.createRow(rowNum++);
			Cell cell = row.createCell(0);
			cell.setCellValue(i + 1);
			colNum = 1;
			for (int j = 0; j < solutions.size(); j++) {
				cell = row.createCell(colNum++);
				cell.setCellValue(solutions.get(j).getContractor().getContractorId());
				cell = row.createCell(colNum++);
				cell.setCellValue(solutions.get(j).getExecutedDate().toString());
			}
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
