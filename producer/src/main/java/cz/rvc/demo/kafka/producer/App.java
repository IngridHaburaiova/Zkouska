package cz.rvc.demo.kafka.producer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * Hello world!
 *
 */
public class App {

	private static final String jsonfilePath = "./resources/chmu.json";
	private static final String producerPropertiesFilePath = "./resources/producer.properties";
	private static final String KAFKA_TOPIC = "rvc.vars.demo";
	private static final String MESSAGE_KEY = "TestKey";

	public static void main(String[] args) throws Exception {

		Properties props = readProducerProperties();

		String jsonFile = readJsonFile();
		// Prepare produce based on properties
		Producer<String, String> producer = new KafkaProducer<String, String>(props);

		for (int i = 0; i < 3; i++) {
			// Define record to send
			ProducerRecord<String, String> data = new ProducerRecord<>(KAFKA_TOPIC, MESSAGE_KEY, jsonFile);

			// Synchronous Message Send
			Future<RecordMetadata> future = producer.send(data);
			RecordMetadata recordData = future.get();
			System.out.printf("Data has been written into kafka partition Nr : %d, and offset Nr: %d \n",
					recordData.partition(), recordData.offset());
			producer.flush();

			// Asynchronous message send
			producer.send(data, new MyProducerCallback());
		}

		// close producer in synchronous
		producer.close();

	}

	private static String readJsonFile() {
		StringBuilder contentBuilder = new StringBuilder();

		try (Stream<String> stream = Files.lines(Paths.get(jsonfilePath), StandardCharsets.UTF_8)) {
			stream.forEach(s -> contentBuilder.append(s).append("\n"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return contentBuilder.toString();
	}

	private static Properties readProducerProperties() {
		Properties prop = new Properties();

		try {
			prop.load(new FileInputStream(producerPropertiesFilePath));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return prop;
	}
}

// Class for event handling after completion of message send asynchronously
class MyProducerCallback implements Callback {

	@Override
	public void onCompletion(RecordMetadata recordMetadata, Exception e) {
		if (e != null) {
			System.err.println("Assynchronnous producer message failed");
		} else {
			System.out.printf("Asynchroinous Data has been written into kafka partition Nr : %d, and offset Nr: %d \n",
					recordMetadata.partition(), recordMetadata.offset());
		}
	}

}
