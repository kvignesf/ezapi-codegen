package com.org.gen.json2pojo.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Jackson2Annotator;
import org.jsonschema2pojo.SchemaGenerator;
import org.jsonschema2pojo.SchemaMapper;
import org.jsonschema2pojo.SchemaStore;
import org.jsonschema2pojo.SourceType;
import org.jsonschema2pojo.rules.RuleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JCodeModel;

/**
 * @author Kash
 *
 */
public class DTOGenDriver {
	
	
	BufferedReader reader = null;
	File fileGenerated2 = null;
	String projectid = "";
	String dtoName = "";
	String basePath = "";
	String pkgPath = "";
	String sourceBasepath = "";
	String targetBasepath = "";
	String projPath = "";
	String projBasePath = "";
	String targetfullPath = "";
	String targetfulldtoPath = "";
	File templateFile = null;
	
	private ResourceLoader resourceLoader;
    private static final Logger logger = LoggerFactory.getLogger(DTOGenDriver.class);

	@Autowired
	public DTOGenDriver(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public DTOGenDriver() {
		// TODO Auto-generated constructor stub
	}

	public String updateInputFile(JsonNode payLoadInput, Map<String, String> reqInputTokens) {
		//FileWriter writer = null;
		String oldContent = "";
		System.out.println("reqInputTokens" + reqInputTokens);
		projectid = reqInputTokens.get("projectid").replaceAll("^\"|\"$", "");
		dtoName = reqInputTokens.get("name").replaceAll("^\"|\"$", "");
		System.out.println("projectid.." + projectid);
		System.out.println("name.." + dtoName);
		String returnMsg = "Failure";
		String prgrmType = "/javacode";
		
		try {
			File baseLocationofFile = getBaseLocation("src/main/resources");			
			ClassPathResource cpr = new ClassPathResource("schemas/input.json");
			InputStream jsonFileAsStream = cpr.getInputStream();
			if (System.getProperty("os.name").contains("Windows")) {
				basePath = "C:\\workspace\\pojodtogen\\";
				fileGenerated2 = new File(basePath + baseLocationofFile + File.separator
						+ "schemas" + File.separator + dtoName.toString() + ".json");
				fileGenerated2.createNewFile();
				pkgPath = "com\\ezapi\\*";
				//sourceBasepath = "";
				//targetBasepath = " C:\\Users\\krish\\Documents\\output\\";
				
				projBasePath = "/src/main/java/com/ezapi/api/";
				projPath = projBasePath+ "service/dto";
                
				sourceBasepath = "";
				targetBasepath = "C:/ezapi/codegentemplates/javatmplts/target1/";
				targetfullPath = targetBasepath+projectid+prgrmType+projBasePath;
				targetfulldtoPath = targetBasepath+projectid+prgrmType+projPath;
			} else {
				//basePath = "/tmp/ezapi_dto_code_gen_test/ezapi_dto_generator/";
				basePath = "/var/app/ezapi_codegen/ezapi_dto_generator/";
				/*fileGenerated2 = new File(basePath + baseLocationofFile + File.separator
						+ "schemas" + File.separator + dtoName.toString() + ".json"); */
				//fileGenerated2.createNewFile();
				pkgPath = "com/ezapi";
				projBasePath = "/src/main/java/com/ezapi/api/";
				projPath = projBasePath+ "service/dto";
                
				sourceBasepath = "";
				targetBasepath = " /mnt/codegen/";
				targetfullPath = targetBasepath+projectid+prgrmType+projBasePath;
				targetfulldtoPath = targetBasepath+projectid+prgrmType+projPath;
				
				basePath = "/var/app/ezapi_java_code_gen/";
				fileGenerated2 = new File(basePath + "src/main/resources" + File.separator
						+ "schemas" + File.separator + dtoName.toString() + ".json");
				fileGenerated2.createNewFile();
				baseLocationofFile = getBaseLocation(basePath+ "src/main/resources");	
				cpr = new ClassPathResource("/var/app/ezapi_java_code_gen/src/main/resources/schemas/input.json");
				jsonFileAsStream = cpr.getInputStream();
			}

			reader = new BufferedReader(new InputStreamReader(jsonFileAsStream));
			// Reading all the lines of input text file into oldContent
			String line = reader.readLine();
			while (line != null) {
				oldContent = oldContent + line + System.lineSeparator();
				line = reader.readLine();
			}
			System.out.println("old.."+oldContent);
			System.out.println(".new.." + payLoadInput);
			String newContent = oldContent.replaceAll("replace", payLoadInput.toString());
			System.out.println("newContent.." + newContent);
			System.out.println("fniished..");
			JSONParser parser = new JSONParser();
			JSONObject jsonobj = (JSONObject) parser.parse(newContent);
			System.out.println("..jsonobj.." + jsonobj.toString());
			
			Files.write(Paths.get(fileGenerated2.toURI()), jsonobj.toJSONString().getBytes());
			
			String packageName = "com.ezapi";
			String respMsg = "";


			//File inputJson = new File(baseLocationofFile + File.separator + "schemas" + File.separator + "output.json");
			File outputPojoDirectory = new File(baseLocationofFile + File.separator + "convertedPojo2");
			outputPojoDirectory.mkdirs();
			System.out.println("..output.."+outputPojoDirectory.getPath());
			try {
				convert2JSON(fileGenerated2.toURI().toURL(), outputPojoDirectory, packageName,fileGenerated2.getName().replace(".json", ""));				
				System.out.println(".." + System.getProperty("os.name"));
				if (System.getProperty("os.name").contains("Windows")) {
					String currdir = runCommand("cmd", "/c", "cd");
					System.out.println("currdir.."+currdir);					
					String fileGenerated = runCommand("cmd", "/c", "dir /b " + outputPojoDirectory.getPath().toString()+"\\"+pkgPath);
					System.out.println("..fileGenerated,," + fileGenerated);
                    //runCommand("cmd", "/c", "mkdir  C:\\Users\\krish\\Documents\\output\\" +projectid);
					//runCommand("cmd", "/c", "move " +outputPojoDirectory.getPath().toString()+"\\"+pkgPath + " C:\\Users\\krish\\Documents\\output\\"+projectid);
					runCommand("cmd", "/c", "move " +currdir+"\\"+outputPojoDirectory.getPath().toString()+"\\"+pkgPath + " " + targetfulldtoPath);
					//String fileCopied = runCommand("cmd", "/c", "dir /b " + " C:\\Users\\krish\\Documents\\output\\"+projectid);
					String fileCopied = runCommand("cmd", "/c", "dir /b " + targetfulldtoPath);
					System.out.println("..fileCopied,," + fileCopied);
					if (!isNullOrEmpty(fileCopied) && !isNullOrEmpty(fileGenerated)) {
						if (fileCopied.equalsIgnoreCase(fileGenerated)) {
							returnMsg = "Success";
						} else {
							returnMsg = "Failed to move the generated files";
						}
					}
				} else if (System.getProperty("os.name").contains("Linux")) {
					String currdir = runCommand("sh", "-c", "pwd");
					System.out.println("currdir.."+currdir);
                    System.out.println("..dir.."+outputPojoDirectory.getPath().toString());
                    //String projPath = "/src/main/java/com/ezapi/api/service/dto";
                    //String projBasePath = "/src/main/java/com/ezapi/api/";
                    respMsg=runCommand("sh", "-c", "rm -rf " + " /mnt/codegen/"+projectid+prgrmType+projBasePath+"*JHipster.java");
                    System.out.println("respMsg.."+respMsg);                    
					String fileGenerated = runCommand("sh", "/c", "ls *.java | tr '\n' '\n' " + currdir + outputPojoDirectory.getPath().toString()+"/"+pkgPath);
                    runCommand("sh", "-c", "mv " +currdir+"/"+outputPojoDirectory.getPath().toString()+"/"+pkgPath+"/*"+ " /mnt/codegen/"+projectid+prgrmType+projPath);
					String fileCopied = runCommand("sh", "/c", "ls *.java | tr '\\n' '\\n' " + " /mnt/codegen/"+projectid+prgrmType+projPath);
					System.out.println("..fileCopied,," + fileCopied);
					if (!isNullOrEmpty(fileCopied) && !isNullOrEmpty(fileGenerated)) {
						if (fileCopied.equalsIgnoreCase(fileGenerated)) {
							returnMsg = "Success";
						} else {
							returnMsg = "Failed to move the generated files";
						}
					}                    
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Encountered issue while converting to pojo: " + e.getMessage());
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// Closing the resources
				reader.close();
				// writer.close();

			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return returnMsg;
	}
	
	
	public String genJavaCodeThruJHip(String projectId, String outputFile) {
		String baseFilePath = "",srcFilePath = "";
		String controllerPath = "";
		String finalStatus = "failure"; 
		String parentDirectory = "";
				
		System.out.println("..outputFile.."+outputFile);
		File f = new File(outputFile);
		System.out.println("..fileparent.." + f.getPath() + "..." + f.getParentFile());
		parentDirectory = f.getParentFile().toString();
		try {
			//baseFilePath = "C:\\ezapi\\codegentemplates\\javatmplts\\target1\\"+projectId;
			baseFilePath = parentDirectory;
			if (System.getProperty("os.name").contains("Windows")) {
				File tempDirectory = new File(baseFilePath);
				if (tempDirectory.exists()) 
				{ 
					
				} else { 
					Path dirs = Files.createDirectories(Path.of(baseFilePath));
					logger.debug("directories created: "+ dirs);
				}
				
				//baseFilePath = baseFilePath + File.separator + 
				//srcFilePath = "C:\\ezapi\\codegentemplates\\source\\projectResourceApi\\";
				//controllerPath = "\\Controllers\\";
			} else if (System.getProperty("os.name").contains("Linux")) {
				File tempDirectory = new File(baseFilePath);
				if (tempDirectory.exists()) 
				{ 
					
				} else { 
					Path dirs = Files.createDirectories(Path.of(baseFilePath));
					logger.debug("directories created: "+ dirs);
				}
			}
			
			Process processP1 = Runtime.getRuntime().exec("npm.cmd install generator-jhipster@7.0.1 ", null, new File(baseFilePath) );
			printResults(processP1);
			
			/*
			Process processP2 = Runtime.getRuntime().exec("jhipster.cmd jdl --force  "+outputFile, null, new File(baseFilePath) );
			//printResults(processP2);
			processP2.waitFor();
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(processP2.getInputStream()));
			
			BufferedReader stdError = new BufferedReader(new InputStreamReader(processP2.getErrorStream()));
			String s = null;
			while ((s = stdInput.readLine()) != null)
	           {
	              System.out.println(s);
	           }
	          // read any errors from the attempted command
	          System.out.println("Here is the standard error of the command (if any):\n");
	           
	          while ((s = stdError.readLine()) != null)
	          {
	               System.out.println(s);
	          }
			
			//String vldmsg = runCommand("cmd.exe", "/c", "jhipster.cmd jdl --force  "+outputFile );
			//System.out.println("vldmsg.."+vldmsg);
			*/
			
			
			ProcessBuilder builder = new ProcessBuilder();
			if (System.getProperty("os.name").contains("Windows")) {
			    builder.command("cmd.exe", "/c", "jhipster.cmd jdl --force  "+outputFile);
			} else {
			    builder.command("sh", "-c", "ls");
			}
			builder.directory(new File(baseFilePath));
			builder.inheritIO();
			Process process = builder.start();
			StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
			Future<?> future = Executors.newSingleThreadExecutor().submit(streamGobbler);
			int exitCode = process.waitFor();
			assert exitCode == 0;
			future.get(180, TimeUnit.SECONDS);
			Thread.sleep(5000);
			//String destPath = baseFilePath+codeProjName+File.separator+"Models";
			File tempDirectory0 = new File(baseFilePath+"\\"+"src");
			if (tempDirectory0.exists()) 
			{ 
				finalStatus = "success";
			}
		}  catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return finalStatus;
	}
		
		private static boolean isNullOrEmpty(String str){
		    if(str == null || str.isEmpty())
		      return true;
		    return false;
		  }
		
	public void convert2JSON(URL inputJson, File outputPojoDirectory, String packageName, String className)
			throws IOException {
		JCodeModel codeModel = new JCodeModel();
		URL source = inputJson;
		GenerationConfig config = new DefaultGenerationConfig() {
			@Override
			public boolean isGenerateBuilders() { // set config option by overriding method
				return true;
			}

			public SourceType getSourceType() {
				return SourceType.JSONSCHEMA;
			}
		};
		SchemaMapper mapper = new SchemaMapper(
				new RuleFactory(config, new Jackson2Annotator(config), new SchemaStore()), new SchemaGenerator());
		mapper.generate(codeModel, className, packageName, source);
		codeModel.build(outputPojoDirectory);
	}
	
	public static void printResults(Process process) throws IOException {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	    String line = "";
	    System.out.println("..line reader.." + reader.readLine());
		
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
			logger.debug("line.." + line);
		} 
	    
		/*
		 * while (!reader.readLine().contains("Congratulations")) {
		 * System.out.println(line); logger.debug("line.."+ line); }
		 */
	}

	public static File getBaseLocation(String folderName) {
		File baseLocationFile = new File(folderName);
		if (!baseLocationFile.exists()) {
			baseLocationFile = loadFromClassPath(folderName, baseLocationFile);
		}
		if (!baseLocationFile.exists()) {
			System.out
					.println("Base location file provided does nt exists in the classpath/filesystem {}" + folderName);
		}
		return baseLocationFile;
	}

	private static File loadFromClassPath(String baseLocation, File baseLocationFile) {

		URL resource = DTOGenDriver.class.getClassLoader().getResource(baseLocation);
		if (resource != null) {
			baseLocationFile = new File(resource.getFile());
		}
		return baseLocationFile;
	}
	
	public String runCommand(String... command) {
		ProcessBuilder processBuilder = new ProcessBuilder().command(command);
		//System.out.println("command.."+command.toString());
		Process process;
		String output = "";
		String returnDir = "";
		int returnStrLeng = 0;
		try {
			process = processBuilder.start();
			InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			while ((output = bufferedReader.readLine()) != null) {
				System.out.println(output);
				returnDir = output + "," + returnDir;
			}
			process.waitFor();
			bufferedReader.close();
			process.destroy();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("..returnDir.."+returnDir);
		if (returnDir.length() > 0) {
			returnDir = returnDir.substring(0, returnDir.length()-1);
		}
		//System.out.println(",returnDir.."+returnDir.substring(0, returnDir.length()-1));
		//return returnDir.substring(0, returnDir.length()-1);
		return returnDir;
	}

}
