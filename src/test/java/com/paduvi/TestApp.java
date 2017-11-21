package com.paduvi;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paduvi.app.Main;
import com.paduvi.app.models.Contractor;
import com.paduvi.app.models.Owner;

public class TestApp {

	public static Owner importOwnerFromJson(File file) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Owner owner = null;

		try {
			owner = mapper.readValue(file, Owner.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return owner;
	}

	public static Contractor importContractorFromJson(File file) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Contractor contractor = null;

		try {
			contractor = mapper.readValue(file, Contractor.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return contractor;
	}

	public static void main(String[] args) {
		File folder = new File(TestApp.class.getResource("/data_sample").getPath());

		File[] files = folder.listFiles((dir, name) -> {
			return name.startsWith("sample_contractor_") && name.endsWith(".json");
		});
		List<Contractor> contractors = Arrays.asList(files).parallelStream().map(file -> importContractorFromJson(file))
				.collect(Collectors.toList());

		File file = new File(TestApp.class.getResource("/data_sample/sample_owner.json").getPath());
		Owner owner = importOwnerFromJson(file);

		Main app = new Main(owner, contractors);
		app.printResult();
	}
}
