package com.paduvi;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.paduvi.app.Main;
import com.paduvi.app.models.Contractor;
import com.paduvi.app.models.Project;
import com.paduvi.app.models.Timestamp;
import com.paduvi.app.models.TimestampDeserializer;

public class TestApp {

	public static void main(String[] args) throws Exception {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
		gsonBuilder.registerTypeAdapter(Timestamp.class, new TimestampDeserializer());
		Gson gson = gsonBuilder.create();
		File file = new File(TestApp.class.getResource("/data_sample/da1.json").getPath());

		JsonObject json = gson.fromJson(new FileReader(file), JsonObject.class);
		Project project = gson.fromJson(json, Project.class);

		List<Contractor> contractors = gson.fromJson(json.get("contractors").toString(),
				new TypeToken<List<Contractor>>() {
				}.getType());

		Main app = new Main(project, contractors);
		app.printResult();
	}
}
