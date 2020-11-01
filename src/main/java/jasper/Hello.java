package jasper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.text.DocumentException;

import jasper.cert.Pkcs;
import jasper.utils.ImageUtil;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@EnableAutoConfiguration
@SpringBootApplication
@Controller
public class Hello {

	@RequestMapping({ "/he" })
	@ResponseBody
	public String firstPage() {
		return "Hello World";
	}
	
	@RequestMapping(value="/imagedencode/{base64Images}")
	@ResponseBody
	public String creatImageBase64Code(@PathVariable String base64Image) {
		String filePath = "/Users/chenhui/springwork/jasper/src/main/resources/";
		 ImageUtil.decoder(base64Image, filePath+"c.png");
		 return "convert to image success";
	}

	@RequestMapping({ "/imagecode" })
	@ResponseBody
	public String getImageBase64Code() {
		String filePath = "/Users/chenhui/springwork/jasper/src/main/resources/";
		String c =  ImageUtil.encoder(filePath+"g.png");
		ImageUtil.decoder(c, filePath+"c.jpg");
		return c;
	}
	
	@RequestMapping({ "/cert" })
	@ResponseBody
	public String signCert() throws IOException {
		String filePath = "/Users/chenhui/springwork/jasper/src/main/resources/";
	String issuerStr = "CN=在线医院,OU=gitbook研发部,O=gitbook有限公司,C=CN,E=gitbook@sina.com,L=北京,ST=北京";
    String subjectStr = "CN=huangjinjin,OU=gitbook研发部,O=gitbook有限公司,C=CN,E=huangjinjin@sina.com,L=北京,ST=北京";
    String certificateCRL  = "https://gitbook.cn";
    Map<String, byte[]> result = Pkcs.createCert("123456", issuerStr, subjectStr, certificateCRL);

    FileOutputStream outPutStream = new FileOutputStream(filePath+"keystore.p12"); // ca.jks
    outPutStream.write(result.get("keyStoreData"));
    outPutStream.close();
    FileOutputStream fos = new FileOutputStream(new File(filePath +"keystore.cer"));
    fos.write(result.get("certificateData"));
    fos.flush();
    fos.close();
	return "sign cert success";
	}

	@RequestMapping({ "/addimage" })
	@ResponseBody
	public String addImagetoPdf() throws FileNotFoundException, GeneralSecurityException, IOException, DocumentException {
		
		String filePath = "/Users/chenhui/springwork/jasper/src/main/resources/";
	        String KEYSTORE= filePath+"keyStore.p12";//"H://pcs12//houKeyStore";
	        char[] PASSWORD = "123456".toCharArray();//keystory密码
	        String SRC=filePath+"testtable.pdf";//H://pcs12//testPage123.pdf" ;//原始pdf
//	        String SRC=filePath+"a2.pdf";
	        String DEST=filePath+"addimage.pdf";//H://pcs12//demo_signed_box.pdf" ;//签名完成的pdf
//	        String DEST2=filePath+"addimage2.pdf";//"H://pcs12//demo_signed_itext.pdf" ;//签名完成的pdf
	        String chapterPath=filePath+"g.png";//"H://pcs12//d4f894aaef66f07181e55a1b910cf4f5.jpg";//签章图片
	        String reason="数据不可更改";
	        String location="桃源乡";
	 
	        PdfUtil.sign(new FileInputStream(SRC), new FileOutputStream(DEST),
	                new FileInputStream(KEYSTORE), PASSWORD,
	                reason, location, chapterPath);
	   
	      
	        
	        System.out.println("签章完成 ");

	   
		return "add image to pdf success";
	}
	
	
	@RequestMapping(value="/upload", method=RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseBody
	public String fileUpload(@RequestParam("file") MultipartFile file) throws IOException {
		
		File convertFile = new File("/var/tmp/"+file.getOriginalFilename());
		System.out.println(convertFile.getName());
	
		convertFile.createNewFile();

	FileOutputStream fout = new FileOutputStream(
			convertFile);
	fout.write(file.getBytes());
	fout.close();
	return "File is upload successfully";
}
	
	
	@RequestMapping({ "/restUp" })
	@ResponseBody
	public String restTemplateUploadFile() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		MultiValueMap<String,Object> body = new LinkedMultiValueMap<>();
		 
		String filename = "/Users/chenhui/springwork/jasper/src/main/resources/testtable.pdf";
		FileSystemResource file = new FileSystemResource(new File(filename));
		
		body.add("file",file);
		
		HttpEntity<MultiValueMap<String,Object>> requestEntity = new HttpEntity<>(body,headers);
		
		String serverUrl =  "http://localhost:8080/upload";
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = restTemplate.postForEntity(serverUrl, requestEntity, String.class);
		return response.getBody();
	}
	
	
	@RequestMapping(value="/download", method=RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Object> downloadFile() throws IOException {
		System.out.print("ooooooooooooooooooooooooooooooooo");
		String filename = "/Users/chenhui/springwork/jasper/src/main/resources/testtable.pdf";
		
		File file = new File(filename);
		InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getName()));
		headers.add("Cache-Control", "no-cache, no-store, must- revalidate");
		headers.add("Pragma", "no-cache"); headers.add("Expires", "0");
		ResponseEntity<Object> responseEntity = ResponseEntity.ok()
				.headers(headers).contentLength(file.length())
				.contentType(MediaType.parseMediaType("application/txt"))
				.body(resource);
		
		return responseEntity;
}
	
	
	

	@RequestMapping({ "/tb" })
	@ResponseBody
	public String getUsers() {
		try {

			String sourcePath = "/Users/chenhui/springwork/jasper/src/main/resources/";
			Map<String, Object> parameters = new HashMap<>();

			List<User> users = new ArrayList<User>();
			for (int i = 5; i < 10; i++) {
				List<City> cities = new ArrayList<>();

				if (i % 2 == 0) {
					City c = new City("code1", "cityname1");
					cities.add(c);
					City c2 = new City("code2", "cityname2");
					cities.add(c2);
					City c3 = new City("code3", "cityname3");
					cities.add(c3);
				}
				User user = new User(cities, i, "name" + i, i + 30);
				users.add(user);
			}
			parameters.put("users", users);
			JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(users);

			String printFileName = JasperFillManager.fillReportToFile(sourcePath + "testtable.jasper", parameters,
					beanColDataSource);

			System.out.println(printFileName);
			if (printFileName != null) {
				/**
				 * 1- export to PDF
				 */
				JasperExportManager.exportReportToPdfFile(printFileName, sourcePath + "testtable.pdf");
				System.out.println(sourcePath + "testtable.pdf");
			}
		} catch (JRException e) {
			e.printStackTrace();
		}
		return "success";

	}

	@GetMapping("/r")
	@ResponseBody
	public String generateReport() {
		try {

			String sourcePath = "/Users/chenhui/springwork/jasper/src/main/resources/";
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("subdir", sourcePath);

			List<User> users = new ArrayList<User>();
			for (int i = 0; i < 10; i++) {
				List<City> cities = new ArrayList<>();

				if (i % 2 == 0) {
					City c = new City("code1", "cityname1");
					cities.add(c);
					City c2 = new City("code2", "cityname2");
					cities.add(c2);
					City c3 = new City("code3", "cityname3");
					cities.add(c3);
				}
				User user = new User(cities, i, "name" + i, i + 30);
				users.add(user);
			}

			JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(users);

			String printFileName = JasperFillManager.fillReportToFile(sourcePath + "test.jasper", parameters,
					beanColDataSource);
			if (printFileName != null) {
				/**
				 * 1- export to PDF
				 */
				JasperExportManager.exportReportToPdfFile(printFileName, sourcePath + "test.pdf");

//		            /**
//		             * 2- export to HTML
//		             */
//		            JasperExportManager.exportReportToHtmlFile(printFileName,
//		               "C://sample_report.html");
//
//		            /**
//		             * 3- export to Excel sheet
//		             */
//		            JRXlsExporter exporter = new JRXlsExporter();
//
//		            exporter.setParameter(JRExporterParameter.INPUT_FILE_NAME,
//		               printFileName);
//		            exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME,
//		               "C://sample_report.xls");
//
//		            exporter.exportReport();
			}
		} catch (JRException e) {
			e.printStackTrace();
		}
		return "success";

	}

//			// Compile the Jasper report from .jrxml to .japser
//			JasperReport jasperReport = JasperCompileManager.compileReport(reportPath + "/test.jrxml");
//
	// Get your data source
//			JRBeanCollectionDataSource jrBeanCollectionDataSource = new JRBeanCollectionDataSource(users);
//			InputStream employeeReportStream
//			  = getClass().getResourceAsStream("/test.jrxml");
//			JasperReport jasperReport
//			  = JasperCompileManager.compileReport(employeeReportStream);
//			JRSaver.saveObject(jasperReport, reportPath+"/test.jasper");
//			JasperExportManager.exportReportToPdfFile(jasperPrint, reportPath + "rpt.pdf");

//			HtmlExporter exporter = new HtmlExporter();
//			 
//			// Set input ...
//			exporter.setExporterOutput(
//			  new SimpleHtmlExporterOutput("test.html"));
//			 
//			exporter.exportReport();
//			// Add parameters
//			Map<String, Object> parameters = new HashMap<>();
//
//			parameters.put("createdBy", "Websparrow.org");
////
////			// Fill the report
//			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters
//					);
////
////			// Export the report to a PDF file
//			JasperExportManager.exportReportToPdfFile(jasperPrint, reportPath + "rpt.pdf");
//
//			System.out.println("Done");
//
//			return "Report successfully generated @path= " + reportPath;
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			return e.getMessage();
//		}
//	}
//	
//	@GetMapping("/report")
//	public String empReport() {
//
//		InputStream employeeReportStream
//		  = getClass().getResourceAsStream("/test.jrxml");
//		JasperReport jasperReport
//		  = JasperCompileManager.compileReport(employeeReportStream);
//		JRSaver.saveObject(jasperReport, "test.jasper");
//
//		HtmlExporter exporter = new HtmlExporter();
//		 
//		// Set input ...
//		exporter.setExporterOutput(
//		  new SimpleHtmlExporterOutput("test.html"));
//		 
//		exporter.exportReport();
//	}

	public static void main(String[] args) {
		SpringApplication.run(Hello.class, args);
	}
}