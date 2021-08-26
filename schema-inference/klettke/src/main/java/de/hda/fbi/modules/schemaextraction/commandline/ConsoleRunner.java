package de.hda.fbi.modules.schemaextraction.commandline;

import de.hda.fbi.modules.schemaextraction.AnalyseManager;
import de.hda.fbi.modules.schemaextraction.DecisionManager;
import de.hda.fbi.modules.schemaextraction.DecisionTableCreator;
import de.hda.fbi.modules.schemaextraction.EntityReader;
import org.springframework.context.annotation.Profile;
import picocli.CommandLine;
import de.hda.fbi.modules.schemaextraction.ExtractionFacade;
import de.hda.fbi.modules.schemaextraction.TreeIntegrationManager;
import de.hda.fbi.modules.schemaextraction.common.AnalyseResult;
import de.hda.fbi.modules.schemaextraction.common.ExtractionResult;
import de.hda.fbi.modules.schemaextraction.common.SchemaExtractionException;
import de.hda.fbi.modules.schemaextraction.common.SchemaExtractionUseCase;
import de.hda.fbi.modules.schemaextraction.configuration.DatabaseConfigurationBuilder;
import de.hda.fbi.modules.schemaextraction.configuration.SchemaExtractionConfiguration;
import de.hda.fbi.modules.schemaextraction.configuration.SchemaExtractionConfigurationBuilder;
import de.hda.fbi.modules.schemaextraction.decision.DecisionCommand;
import de.hda.fbi.modules.schemaextraction.decision.DecisionTable;
import de.hda.fbi.modules.schemaextraction.decision.DecisionTableEntry;
import de.hda.fbi.modules.schemaextraction.impl.AnalyseManagerImpl;
import de.hda.fbi.modules.schemaextraction.impl.DecisionManagerImpl;
import de.hda.fbi.modules.schemaextraction.impl.DecisionTableCreatorImpl;
import de.hda.fbi.modules.schemaextraction.impl.ExtractionFacadeImpl;
import de.hda.fbi.modules.schemaextraction.impl.TreeIntegrationManagerImpl;
import de.hda.fbi.modules.schemaextraction.mongodb.MongoEntityReader;
import de.hda.fbi.modules.schemaextraction.tree.TreeNodeInfoService;
import de.hda.fbi.modules.schemaextraction.tree.TreeNodeInfoServiceImpl;
import de.hda.fbi.modules.schemaextraction.tree.TreeNodeManager;
import de.hda.fbi.modules.schemaextraction.tree.TreeNodeManagerImpl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

// import de.hda.fbi.
// Diese Konsolenanwendung soll das Zusammenspiel zwischen einer beliebigen Kompononte (hier eine
// Konsole) und der Schema-Extraktions-Komponente verdeutlichen.
// Zunächst muss die SchemaExtractionConfiguration mit allen wichtigen Informationen befüllt werden.
// Anschließend wird die SE-Komponente mit dem jeweiligen UseCase aufgerufen.
// Bei einer initialen oder inkrementellen SE erstellt die SE-Komponente eine DecisionTable
// Ist diese DecisionTable ohne Konfikte wird sie ohne weitere Rückmeldung ausgegeben
// Sind allerdings Konflikte vorhanden müssen diese Schritt für Schritt geklärt werden
// Die jeweilige Nummer hinter #### steht dann für den jeweiligen Eintrag.
// Bei Copy und MoveCommands müssen noch Join-Bedingungen angegeben werden. Dies erfolgt NACHDEM sich
// für ein Copy oder Move entschieden wurde.
// Die Auswahl der Join-Bedingung kann sicherlich noch optimiert werden. Die Konsolenanwendung hier
// ist lediglich ein grobes Gerüst, damit spätere Entwickler den Ablauf nachvollziehen können.
// Nachdem alle Konflikte geklärt wurden, wird die DecisionTable ausgegeben
//
@ComponentScan(basePackages = {"de.hda.fbi.modules.schemaextraction"})
@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class,
	ProjectInfoAutoConfiguration.class})
@Profile("gitlab")
public class ConsoleRunner implements CommandLineRunner {

	public static final int PERFORMACE_EXECUTIONS = 30;

//	@Autowired
//	private ExtractionFacade facade;
	private static ExtractionFacade WIRED_FACADE;

	@Autowired
	private ApplicationContext appContext;
//	@Autowired
	private static ArgumentsParser argsParser = new ArgumentsParser();

	public static void main(String args[]) throws SchemaExtractionException {

		EntityReader entityReader = new MongoEntityReader();
		TreeNodeManager treeNodeManager = new TreeNodeManagerImpl();
		TreeNodeInfoService treeNodeInfoService = new TreeNodeInfoServiceImpl();

		DecisionManager decisionManager = new DecisionManagerImpl();
		DecisionTableCreator decisionTableCreator = new DecisionTableCreatorImpl(treeNodeInfoService, null, null);
		AnalyseManager analyseManager = new AnalyseManagerImpl(treeNodeInfoService);
		TreeIntegrationManager treeIntegrationManager = new TreeIntegrationManagerImpl(entityReader, treeNodeManager);

		WIRED_FACADE = new ExtractionFacadeImpl(decisionManager, decisionTableCreator, analyseManager, treeIntegrationManager);

		new CommandLine(argsParser).parse(args);
		if (argsParser.isHelp()) {
			CommandLine.usage(new ArgumentsParser(), System.out);
			return;
		}
		if (argsParser.isFileAvailable()) {
			argsParser = FileHandler.parseConfigFile(argsParser.getFile());
		}

		ConsoleRunner runner = new ConsoleRunner();
		runner.run(args);

//        SpringApplication.run(ConsoleRunner.class, args);
	}

	private void initiateShutdown(int returnCode) {
		SpringApplication.exit(appContext, () -> returnCode);
	}

	private static void printDecisionTable(DecisionTable decisionTable, ExtractionFacade facade) {

		List<DecisionTableEntry> decisionEntries = decisionTable.decisionEntries;

		for (DecisionTableEntry entry : decisionEntries) {
			if (entry.isClarified()) {
				// Ist ein Eintrag geklärt, gibt es pro Eintrag nur eine
				// Änderungsoperation (Command)
				if (entry.getDecisionCommands().size() > 0) {
					System.out.println(entry.getDecisionCommands().get(0).getCommand().getCommand().toString());
				}
			} else {

				System.out.println("CONFLICT");
				for (DecisionCommand decisionCommand : entry.getDecisionCommands()) {

					if (decisionCommand.getCommand().getCommand().isCopyPropertyCommand()
							|| decisionCommand.getCommand().getCommand().isMovePropertyCommand()) {
						System.out.println("#### \t" + decisionCommand.getIndex() + "\t"
								+ decisionCommand.getCommand().getCommand());

					} else {
						System.out.println("#### \t" + decisionCommand.getIndex() + "\t"
								+ decisionCommand.getCommand().getCommand());
					}
				}

				int index = selectDecisionIndex();

				DecisionTableEntry decisionTableEntry = decisionTable.getDecisionTableEntry(entry.getId());
				DecisionCommand dCmd = decisionTableEntry.getCommand(index);

				String sourceProperty = "";
				String targetProperty = "";

				// Handelt es sich um ein Copy oder Move-Command?
				if (dCmd.getCommand().getCommand().isCopyPropertyCommand()
						|| dCmd.getCommand().getCommand().isMovePropertyCommand()) {
					System.out.print("WHERE " + dCmd.getSourceEntity() + ".####SOURCE PROPERTY =  "
							+ getPropertyValues(dCmd.getSourceProperties()) + " = " + dCmd.getTargetEntity()
							+ ".####TARGET PROPERTY =  " + getPropertyValues(dCmd.getTargetProperties()) + "\n");
					sourceProperty = selectSourceProperty();
					targetProperty = selectTargetProperty();
				}

				decisionTable = facade
						.clarifiyDecision(decisionTable, entry.getId(), index, sourceProperty, targetProperty)
						.getDecisionTable();
			}
		}
	}

	private static String getPropertyValues(List<String> values) {

		String text = "";

		for (String value : values) {
			text += value + "|";
		}

		int index = text.lastIndexOf(text);
		text.toCharArray()[index] = ' ';

		return text;
	}

	private static String selectSourceProperty() {
		System.out.println("SOURCE PROPERTY:");
		String input = null;
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

		try {
			input = bufferedReader.readLine();
			return input;
		} catch (IOException e1) {
			return selectSourceProperty();
		}
	}

	private static String selectTargetProperty() {
		System.out.println("TARGET PROPERTY:");
		String input = null;
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

		try {
			input = bufferedReader.readLine();
			return input;
		} catch (IOException e1) {
			return selectTargetProperty();
		}
	}

	private static int selectDecisionIndex() {
		System.out.println("\nPlease make a decision");
		String input = null;
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		try {
			input = bufferedReader.readLine();
			int index = Integer.parseInt(input);
			return index;
		} catch (IOException e1) {
			return selectDecisionIndex();
		}
	}

	private static AnnotationConfigApplicationContext getContext() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.scan("de.hda.");
		context.refresh();

		return context;
	}

	private static int selectUseCase() {
		System.out.println("");
		System.out.println("#######################################################");
		System.out.println("                          MENU                         ");
		System.out.println("#######################################################");
		System.out.println("");
		System.out.println("Options:");
		System.out.println("\t 1: Schema Overview");
		System.out.println("\t 2: Initial Schema Version Extraction");
		System.out.println("\t 3. Incremental Schema Version Extraction");
		System.out.println("\t q. Quit");

		boolean inputError = false;
		int input = 0;

		do {
			try {
				Scanner scanner = new Scanner(System.in);
				String line = scanner.nextLine();
				line = line.toLowerCase();

				switch (line) {
					case "1":
						input = 1;
						break;
					case "2":
						input = 2;
						break;
					case "3":
						input = 3;
						break;
					case "q":
						input = -1;
						break;
					default:
						throw new InputMismatchException();
				}
				inputError = false;
			} catch (InputMismatchException e) {
				System.out.println("Invalid input. Available options are: 1, 2, 3 or q (Quit)");
				inputError = true;
			}
		} while (inputError);

		return input;
	}

	@Override
	public void run(String... args) throws SchemaExtractionException {
//        do {
//		int useCase = selectUseCase();
		int useCase = 1;
		/* Quit */
//            if (useCase == -1) {
//                break;
//            }

		SchemaExtractionConfiguration extractionConfiguration = new SchemaExtractionConfigurationBuilder()
				.forEntityTypes(argsParser.getEntityTypes())
				.withDatabaseConfiguration(new DatabaseConfigurationBuilder()
						.with(argsParser.getHost(),
								argsParser.getPort(),
								argsParser.getAuthDbName(),
								argsParser.getUsername(),
								argsParser.getPassword())
						.build())
				.withTimestampIdentifier(argsParser.getTsIdentifier())
				.withDatabaseName(argsParser.getDbName())
				.withUseCase(useCase)
				.build();

		List<Long> times = new ArrayList<>();
		ExtractionResult result = null;
		for (int index = 0; index < PERFORMACE_EXECUTIONS; ++index) {
			System.out.println("EXECUTING... " + index);
			long start = System.currentTimeMillis();
			result = WIRED_FACADE.start(extractionConfiguration);
			long executionTime = System.currentTimeMillis() - start;
			System.out.println("FINISHED: " + executionTime + " ms");
			times.add(executionTime);
		}

		System.out.println("TIMES: " + times);

		// Bei der initialen und der inkrementellen Schema-Extraktion ist die
		// DecisionTable relevant
		if (extractionConfiguration.getUseCase() != SchemaExtractionUseCase.AnalyseDatabase) {
			System.out.println("CASE A");
			DecisionTable decisionTable = result.getDecisionTable();

			// Ist die Entscheidungstabelle konfliktfrei?
			if (decisionTable.isClarified()) {
				// Wenn ja, einmal ausdrucken

				System.out.println("Decision Table");
				printDecisionTable(decisionTable, WIRED_FACADE);

				// Mehr muss hier nicht gemacht werden, da hierbei keine
				// Verbindung zu einer SMK besteht
			} else {
				printDecisionTable(decisionTable, WIRED_FACADE);
				System.out.println("\nDecision Table");
				printDecisionTable(decisionTable, WIRED_FACADE);
			}

			List<DecisionTableEntry> decisionEntries = decisionTable.decisionEntries;
			FileHandler.writeEvolutionOperationsToFile(decisionEntries);
			System.out.println("CASE A - END");

		} else {
			System.out.println("CASE B");
			// Bei der Ist-analyse ist das Objekt AnalyserResult relevant
			AnalyseResult analyserResult = result.getAnalyseResult();

			System.out.println("Number of different versions per entity type:");
			for (Map.Entry<String, Integer> map : analyserResult.getNumberOfDifferentSchemasForEntityType().entrySet()) {
				System.out.println(map.getKey() + ": " + map.getValue());
			}

			System.out.println("\n\n\nSchemas:");
			for (String schema : analyserResult.getSchemas()) {
				System.out.println(schema);
			}

			FileHandler.writeSchemataToFile(analyserResult.getSchemas());
			System.out.println("CASE B - END");
		}
		System.out.println("EXIT");
		System.exit(0);
//        } while (true);
//        // Closes SpringBoot Application
//        initiateShutdown(0);
//        System.out.println("SpringBoot Application closed");
//        System.exit(0);
//        System.out.println("Exit code 0");
	}
}
