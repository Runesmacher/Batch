package Runesmacher.Batch;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Runes <runes@sinners.be>
 */
public class CommandHandler {

	HashMap<String, ArrayList<String>> Properties = new HashMap<String, ArrayList<String>>();
	String fileName;
	Logger log = getLogger();
	File file;

	/**
	 * Creates or opens a properties file using specified filename
	 *
	 * @param fileName
	 */
	public CommandHandler(String fileName) {
		this.fileName = fileName;
		file = new File(fileName);

		if (file.exists()) {
			try {
				load();
			} catch (IOException ex) {
				log.severe("[Batch][PropertiesFile] Unable to load " + fileName + "!");
			}
		} else {
			try {
				file.createNewFile();
				Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF8"));
				Date timestamp = new Date();
				writer.write("# Example Properties file generated on " + timestamp.toString() + "\n");
				writer.write("##############\n");
				writer.write("# Map multiple commands to one!\n");
				writer.write("# The player that executes the command must have appropriate permissions for all commands that will be executed\n");
				writer.write("#\n");
				writer.write("# Syntax : all|player:command\n");
				writer.write("#          Define if the command can be run in console and by player (all) or only by the player (player)\n");
				writer.write("#          Add a ; to the last command of the alias\n");
				writer.write("##############\n");
				writer.write("\n");
				writer.write("# /suitup <target>\n");
				writer.write("# Gives the target player a full suit of iron armor and then teleports you to the target\n");
				writer.write("all:suitup\n");
				writer.write("give <<param1>> ironchestplate 1\n");
				writer.write("give <<param1>> ironpants 1\n");
				writer.write("give <<param1>> ironboots 1\n");
				writer.write("give <<param1>> ironhelmet 1;\n");
				writer.write("\n");
				writer.write("# /timeweather <time|day|night> <rain|sun>\n");
				writer.write("# Sets the time to what you want and sets weather\n");
				writer.write("# Due to world specific, only a player can run this command\n");
				writer.write("player:sunnyDay\n");
				writer.write("time set <<param1>>\n");
				writer.write("weather <<param2>>;\n");
				writer.close();
				log.info("[Batch][PropertiesFile] Example properties file created !");
			} catch (IOException ex) {
				log.severe("[Batch][PropertiesFile] Unable to create file " + fileName + "!");
			}
		}
	}

	public boolean load() throws IOException {
		Properties.clear();
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		String line;

		//Cycle through complete contents of the file.         
		while ((line = reader.readLine()) != null) {
			// Check for multiple lines with <<;>> and recreate them as one line.

			if (line.startsWith("#") || line.isEmpty() || line.startsWith("\n") || line.startsWith("\r")) {
				continue;
			}

			String whoAndCommand = line;
			ArrayList<String> subcommandList = new ArrayList<>();

			while (((line = reader.readLine()) != null) && !line.endsWith(";")) {
				subcommandList.add(line);
			}

			if ((line != null) && line.endsWith(";")) {
				subcommandList.add(line.replace(";", ""));
			}
			Properties.put(whoAndCommand, subcommandList);
		}

		reader.close();
		return true;
	}
	
	public String[] getSubCommands(String command) {
		if (Properties.containsKey(command)) {
			ArrayList<String> rt = Properties.get(command);
			return rt.toArray(new String[rt.size()]);
		} else {
			return null;
		}
	}

	public String[] getCommands(CommandSender sender) {
		Set<String> whoAndCommands = Properties.keySet();

		String who;
		String command;
		ArrayList<String> availableCommands = new ArrayList<>();

		for (String whoAndCommand : whoAndCommands) {
			String[] split = whoAndCommand.split("[ \t]*?:[ \t]*?", 2);
			who = split[0];
			switch (who) {
				case "all":
					availableCommands.add(whoAndCommand);
					break;
				case "player":
					if (sender instanceof Player) {
						availableCommands.add(whoAndCommand);
					}
					break;
			}
		}

		String[] commandsArray = new String[availableCommands.size()];
		return availableCommands.toArray(commandsArray);
	}

	public boolean commandExists(java.lang.String command, CommandSender sender) {
		return Properties.containsKey(getWhoAndCommand(command, sender));
	}

	public boolean listCommandsAndSubCommands(CommandSender sender) {
		for (String commandInList : getCommands(sender)) {
			String[] split = commandInList.split("[ \t]*?:[ \t]*?", 2);
			sender.sendMessage(split[1] + ":");
			sender.sendMessage(getSubCommands(commandInList));
		}
		return true;
	}

	public boolean execute(String alias, CommandSender sender, String[] args) {
		String whoAndCommand = getWhoAndCommand(alias, sender);

		ArrayList<Integer> paramCountArray = new ArrayList<>();
		ArrayList<String> toBeExecuted = new ArrayList<>();

		for (String command : getSubCommands(whoAndCommand)) {
			toBeExecuted.add(inputParametersInCommand(command, args));

			int lastParam = command.lastIndexOf("<<param");
			int totalParams = Integer.parseInt(command.substring(lastParam + 7, lastParam + 8));
			paramCountArray.add(totalParams);
		}

		if (args.length - 1 != Collections.max(paramCountArray)) {
			sender.sendMessage("Not enough arguments!");
			return false;
		} else {
			for (String command : toBeExecuted) {
				getServer().dispatchCommand(sender, command);
			}
			return true;
		}
	}

	private String inputParametersInCommand(String command, String[] args) {
		for (int i = 1; i < args.length; i++) {
			command = command.replace("<<param" + i + ">>", args[i]);
		}

		return command;
	}

	private String getWhoAndCommand(String alias, CommandSender sender) {
		String whoAndCommand = "alias";
		if (sender instanceof Player && Properties.containsKey("all:" + alias)) {
			whoAndCommand = "all:" + alias;
		} else if (sender instanceof Player && Properties.containsKey("player:" + alias)) {
			whoAndCommand = "player:" + alias;
		} else {
			whoAndCommand = "all:" + alias;
		}
		return whoAndCommand;
	}
}
