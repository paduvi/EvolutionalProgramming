package com.paduvi.app;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

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

	static final int NUMBER_OF_TASKS = 40;

	static String getBuyerProfit(Project project, List<Solution> solutions) {
		Locale locale = new Locale("vi", "VN");
		NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
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
		return currencyFormatter.format(profit);
	}

	static double getQualityProfit(List<Solution> solutions) {
		double sumQuality = solutions.parallelStream().mapToDouble(solution -> solution.getContractor().getQuality())
				.sum();
		return sumQuality;
	}

	static String getContractorProfit(Contractor contractor, Project project, List<Solution> solutions) {
		Locale locale = new Locale("vi", "VN");
		NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
		double profit = IntStream.range(0, solutions.size()).parallel().mapToDouble(i -> {
			if (solutions.get(i).getContractor().getContractorId() != contractor.getContractorId()) {
				return 0;
			}
			Timestamp executedDate = solutions.get(i).getExecutedDate();
			Pack pkg = project.getPackages().get(i);
			double differentPrice = pkg.getProducts().parallelStream().mapToDouble(p -> {
				int productId = p.getProductId();
				Product temp = contractor.getProducts().parallelStream().filter(p1 -> p1.getProductId() == productId)
						.findAny().get();
				return ((1 - temp.getDiscountRate(executedDate)) * temp.getSellPrice() - temp.getBuyPrice())
						* p.getQuantity();
			}).sum();
			return differentPrice
					* Math.exp(project.getInflationRate() * (executedDate.get() - project.getStartDate().get()) / 7);
		}).sum();
		return currencyFormatter.format(profit);
	}

	static void writeResult(Main result, PrintWriter writer) {
		writer.println("Cân bằng Nash tìm được:");
		writer.println("\tProfit của Chủ đầu tư: " + getBuyerProfit(result.getProject(), result.getSolution()));
		writer.println("\tChất lượng của dự án: " + getQualityProfit(result.getSolution()));
		writer.println();
		for (Contractor contractor : result.getContractors()) {
			writer.println("\tProfit của " + contractor.getDescription() + ": "
					+ getContractorProfit(contractor, result.getProject(), result.getSolution()));
		}
		writer.println();
		for (int i = 0; i < result.getSolution().size(); i++) {
			Solution solution = result.getSolution().get(i);
			writer.println("\tGói thầu " + result.getProject().getPackages().get(i).getPackageId() + ":");
			writer.println("\t\t- Tên nhà thầu: " + solution.getContractor().getDescription());
			writer.println("\t\t- Thời gian: " + solution.getExecutedDate().toString());
		}
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

		List<Main> results = new ArrayList<>();
		CountDownLatch countDownLatch = new CountDownLatch(NUMBER_OF_TASKS);

		ExecutorService taskExecutor = Executors
				.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors(), 1));
		for (int i = 0; i < NUMBER_OF_TASKS; i++) {
			taskExecutor.execute(new Thread() {
				@Override
				public void run() {
					Main main = new Main(project, contractors);
					results.add(main);
					countDownLatch.countDown();// important
				}
			});
		}
		countDownLatch.await();
		System.out.println("Finish tasks");

		File outputFile = new File("report.txt");
		PrintWriter pw = new PrintWriter(new FileWriter(outputFile), true);

		AtomicInteger index = new AtomicInteger(0);
		for (Main result : results) {
			pw.println("\n=== Lần " + index.incrementAndGet() + " ===\n");
			writeResult(result, pw);
		}
		pw.close();

		System.out.println("Done");

	}
}
