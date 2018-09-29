package test.com.app.start;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import main.com.galaxy.logic.*;

public class ProgramTest {

	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
	protected String filePath;

	@Before
	public void setUpStreams() {
		filePath = null;
		System.setOut(new PrintStream(outContent));
		System.setErr(new PrintStream(errContent));
	}

	@After
	public void cleanUpStreams() {
		System.setOut(null);
		System.setErr(null);
	}

	@Test
	public void testProgram() throws IOException{
		InputFileProcessor.ProcessFile(filePath);
		InputFileProcessor.MapTokentoIntegerValue();
		OutputResultProcessor.processReplyForQuestion();
		Assert.assertEquals("how much is pish tegj glob glob ? pish tegj glob glob is 42.0\n" +
				"how many Credits is glob prok Iron ? glob prok Iron is 782.0 Credits\n" +
				"how many Credits is glob prok Gold ? glob prok Gold is 57800.0 Credits\n" +
				"how many Credits is glob prok Silver ? glob prok Silver is 68.0 Credits\n"
				, outContent.toString());
	}

}
