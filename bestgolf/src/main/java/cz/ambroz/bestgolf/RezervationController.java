package cz.ambroz.bestgolf;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping(value = "/action")
public class RezervationController {

	private Log log = LogFactory.getLog(RezervationController.class);

	
	Thread pyThread;

/*	@PostConstruct
	public void init() throws IOException {
		
		pyThread = new Thread() {
			@Override
			public void run() {
				var pb = new ProcessBuilder();

				var pysript = getClass().getClassLoader().getResource("python/run.py").getFile().toString();
				if(new File("/usr/bin/python3.7").exists()) {
					pysript = pysript.replaceAll("!", "").replace("file:", "");
					log.info("Py scritp: " + pysript);
					pb.environment().put("FLASK_APP", pysript);
					
					pb.directory(new File("/usr/bin/").getAbsoluteFile());
					pb.command("/usr/bin/python3.7", "-m" , "flask", "run");
					
				} else if(new File("C:\\soft\\Python\\Python38\\python.exe").exists()) {
					log.info("Py scritp: " + pysript);
					pb.environment().put("FLASK_APP", pysript);
					
					pb.command("C:\\soft\\Python\\Python38\\python.exe", "-m" , "flask", "run");
				} else {
					throw new RuntimeException("NO PYTHON");
				}
				log.info("pystart");
				try {
					var process = pb.start();
					var br = new BufferedReader(new InputStreamReader(process.getInputStream()));
					while (br.readLine() != null) {
						log.info("PY:" + br.readLine());
					}
				} catch (IOException e) {
					log.error(e);
				}
				log.info("pystarted");

			}
		};
		pyThread.start();


		

		
		
		
	}*/
	
	@RequestMapping(value = "heart", method = RequestMethod.GET, produces = { "text/plain;charset=UTF-8" })
	@ResponseBody
	public String doHeart() throws IOException {
		{
	      URL url = new URL("http://localhost:5000/heart");
	      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	      conn.setRequestMethod("GET");
	      BufferedReader rd = new BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
	      String line;
	      while ((line = rd.readLine()) != null) {
	          log.info(line);
	      }
	      rd.close();
	      log.info("Heart OK");
		}
		{
	      URL url = new URL("http://localhost:5000/predict");
	      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	      conn.setRequestMethod("POST");
	      conn.setDoInput(true);
	      conn.setDoOutput(true);
	      var osw = new OutputStreamWriter(conn.getOutputStream());
	      osw.write("input=0");
	      for(int i = 1; i < 12000; i++) {
		      osw.write(",0");	    	  
	      }
	      osw.write('\n');
	      osw.close();
	      BufferedReader rd = new BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
	      String line;
	      while ((line = rd.readLine()) != null) {
	          log.info(line);
	      }
	      rd.close();
	      log.info("Heart2 OK");
		} 
	      return "OK";

		
	}


	@RequestMapping(value = "doRezervation", method = RequestMethod.GET, produces = { "text/plain;charset=UTF-8" })
	public void doRezervation(@RequestParam String golfer, @RequestParam String golfcourse,
			@RequestParam String position, @RequestParam int days, HttpServletResponse response) throws IOException {

		response.setContentType(MediaType.TEXT_PLAIN.toString());
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");

		ServletOutputStream outputStream = response.getOutputStream();
		logToBoth(outputStream, "golfer is " + golfer);
		logToBoth(outputStream, "golfcourse is " + golfcourse);
		logToBoth(outputStream, "position is " + position);
		logToBoth(outputStream, "+days is " + days);

		if (golfer.equals("") || golfcourse.equals("") || position.equals("")) {
			logToBoth(outputStream, "!!! Not all paramets set.");
			logToBoth(outputStream, "END");
		}

		String email = "";
		String heslo = "";
		if (golfer.equals("ambroz")) {
			email = "michal.ambroz@centrum.cz";
			heslo = "ksicht";
		} else if (golfer.equals("koukol")) {
			email = "irzik@centrum.cz";
			heslo = "golfsmichalem";
		}

		logToBoth(outputStream, "Loging as " + golfer);

		Document doc;
		Response jsoupResponse = Jsoup.connect("https://rezervace.bestgolf.cz/prihlaseni/index").data("email", email)
				.data("heslo", heslo).timeout(15000).userAgent("Mozilla").method(Method.POST).execute();
		Map<String, String> cookies = jsoupResponse.cookies();
		doc = jsoupResponse.parse();
		logToBoth(outputStream, jsoupResponse.statusCode() == 200 ? "OK" : "NOK");

		logToBoth(outputStream, "Agreeing with rules");
		jsoupResponse = Jsoup.connect("https://rezervace.bestgolf.cz/moje/souhlas").cookies(cookies).timeout(10000)
				.userAgent("Mozilla").execute();
		cookies.putAll(jsoupResponse.cookies());
		doc = jsoupResponse.parse();
		logToBoth(outputStream, jsoupResponse.statusCode() == 200 ? "OK" : "NOK");

		logToBoth(outputStream, "Rezervation page");
		jsoupResponse = Jsoup.connect("https://rezervace.bestgolf.cz/rezervace").cookies(cookies).userAgent("Mozilla").timeout(10000)
				.execute();
		cookies.putAll(jsoupResponse.cookies());
		doc = jsoupResponse.parse();
		logToBoth(outputStream, jsoupResponse.statusCode() == 200 ? "OK" : "NOK");

		DateTime openingTime = new DateTime().withHourOfDay(8).withMinuteOfHour(0).withSecondOfMinute(0)
				.withMillisOfSecond(0);

		while (new DateTime().isBefore(openingTime)) {
			try {
				long millis = openingTime.getMillis() - new DateTime().getMillis();

				if (millis > 30L * 60L * 1000L) {
					logToBoth(outputStream, "More than 30 minutes... Refresh page less then 30 min before 8am.");
					return;
				}

				millis = Math.max(0, Math.min(millis, 60L * 1000L));

				logToBoth(outputStream, new Date().toString() + " - Waiting....");
				synchronized (this) {
					wait(millis);
				}
			} catch (InterruptedException e) {
			}
		}

		long timestamp = new DateTime().withTimeAtStartOfDay().plusHours(0).plusDays(days).getMillis() / 1000;
		logToBoth(outputStream, "Go Go - Rezervation page");
		long goGoMilis = System.currentTimeMillis();
		outputStream.print("Timestamp is " + timestamp);
		jsoupResponse = Jsoup.connect("http://rezervace.bestgolf.cz/rezervace/hriste/" + golfcourse + "/" + timestamp)
				.cookies(cookies).userAgent("Mozilla").execute();
		cookies.putAll(jsoupResponse.cookies());
		doc = jsoupResponse.parse();
		logToBoth(outputStream, jsoupResponse.statusCode() == 200 ? "OK" : "NOK");

		logToBoth(outputStream, "Go Go - Do reservation");
		long timestamp8 = new DateTime().withTimeAtStartOfDay().plusHours(12).plusDays(days).getMillis() / 1000;
		String timeParam = golfcourse + "_" + position + "_" + timestamp8; // 3_2_1513321200
		jsoupResponse = Jsoup.connect("https://rezervace.bestgolf.cz/rezervace/check/" + timestamp)
				.data("time[]", timeParam).data("id_hriste", golfcourse).cookies(cookies).userAgent("Mozilla")
				.method(Method.POST).execute();
		cookies.putAll(jsoupResponse.cookies());
		doc = jsoupResponse.parse();
		logToBoth(outputStream, jsoupResponse.statusCode() == 200 ? "OK" : "NOK");

		for (int x = 1; x < 4; x++) {
			logToBoth(outputStream, "Captcha image: " + x);
			Response resultImageResponse = Jsoup.connect("https://rezervace.bestgolf.cz/captcha.php?getimg")
					.cookies(cookies).ignoreContentType(true).execute();
			cookies.putAll(resultImageResponse.cookies());
			BufferedImage buffeeredImage = ImageIO.read(new ByteArrayInputStream(resultImageResponse.bodyAsBytes()));
			int[] pixel00 = buffeeredImage.getRaster().getPixel(0, 0, (int[]) null);
			logToBoth(outputStream, "Pixel 0-0 has " + pixel00.length + " colors " + pixel00[0]);
			int[] pixel12 = buffeeredImage.getRaster().getPixel(1, 2, (int[]) null);
			logToBoth(outputStream, "Pixel 1-2 has " + pixel12.length + " colors " + pixel12[0]);

			StringBuffer learningSet = new StringBuffer(150 * 80 * 2);

			double[] vector = new double[80 * 150];
			
			var writer = new StringWriter();

			for (int i = 0; i < 80; i++) {
				for (int j = 0; j < 150; j++) {
					int[] pixel = buffeeredImage.getRaster().getPixel(j, i, (int[]) null);
					int color = pixel[0] == pixel00[0] || pixel[0] == pixel12[0] ? 0 : 1;

					vector[150 * i + j] = color;

					writer.append(color == 0 ? "*" : "O");
					learningSet.append(color + ",");
				}
				writer.append("\n");
			}
			
			
			String captchaResult = "";
			
			long cmilis = System.currentTimeMillis();

		      URL url = new URL("http://localhost:5000/predict");
		      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		      conn.setRequestMethod("POST");
		      conn.setDoInput(true);
		      conn.setDoOutput(true);
		      var osw = new OutputStreamWriter(conn.getOutputStream());
		      osw.write("input=" + (int)vector[0]);
		      for(int i = 1; i < 12000; i++) {
			      osw.write("," + (int)vector[i]);	    	  
		      }
		      osw.write('\n');
		      osw.close();
		      BufferedReader rd = new BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
		      String line;
		      while ((line = rd.readLine()) != null) {
		    	  captchaResult = line;
		      }
		      rd.close();
		      
		      logToBoth(outputStream, "Captha in " + (System.currentTimeMillis() - cmilis) + "ms.");


			

			logToBoth(outputStream, "Making rezervation after " + (System.currentTimeMillis() - goGoMilis) + " millis from gogo.");
			jsoupResponse = Jsoup.connect("https://rezervace.bestgolf.cz/rezervace/check/" + timestamp)
					.data("kontrolni_kod", captchaResult).data("odeslat", "odeslat").cookies(cookies)
					.timeout(10000).userAgent("Mozilla").method(Method.POST).execute();
			cookies.putAll(jsoupResponse.cookies());
			doc = jsoupResponse.parse();
			
			logToBoth(outputStream, writer.toString());
			logToBoth(outputStream, captchaResult);
			logToBoth(outputStream, jsoupResponse.statusCode() == 200 ? "OK" : "NOK");
			
			
			


			Elements select = doc.select("#flash");
			if (select.size() > 0) {
				logToBoth(outputStream, select.get(0).text());
				if (select.get(0).text().contains("Green fee bylo rezervováno")) {
					this.writeSuccess(learningSet.toString(), captchaResult);
					return;
				}
			}
			
			this.writeFailure(learningSet.toString());
			logToBoth(outputStream, "WARN unsuccessfull try");

		}

	}

	private void writeSuccess(String learningSet, String reuslt) throws IOException {
		FileWriter fw = new FileWriter("data/successData.txt", true);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter out = new PrintWriter(bw);
		out.println(IOHumanTransform.appendLine(learningSet,reuslt));
		out.flush();
		out.close();
	}

	private void writeFailure(String learningSet) throws IOException {
		FileWriter fw = new FileWriter("data/failureData.txt", true);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter out = new PrintWriter(bw);
		out.println(learningSet);
		out.flush();
		out.close();
	}

	protected void logToBoth(OutputStream oStream, String logMessage) throws IOException {
		log.info(logMessage);
		oStream.write((new DateTime().toString() + " ").getBytes("UTF-8"));
		oStream.write(logMessage.getBytes("UTF-8"));
		oStream.write("\n".getBytes("UTF-8"));
		oStream.flush();
	}

}
