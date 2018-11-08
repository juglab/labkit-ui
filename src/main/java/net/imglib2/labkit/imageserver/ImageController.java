package net.imglib2.labkit.imageserver;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImageController {
	private static final String template = "Hello, %s!";

	@RequestMapping("/greeting")
	public DvidImageMetadata greeting(@RequestParam(value="name", defaultValue="World") String name) {
		return new DvidImageMetadata(new long[]{100, 90, 50});
	}
}
