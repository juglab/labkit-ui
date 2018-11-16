package net.imglib2.ilastik_mock_up;

import java.io.IOException;

public class Main {

	public static void main(String... args) {
		try(Server server = new Server()) {
			System.out.println("Press any key to exit");
			ExampleClient.run();
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
