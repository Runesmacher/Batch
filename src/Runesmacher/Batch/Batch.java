package Runesmacher.Batch;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Runes <runes@sinners.be>
 */
public final class Batch extends JavaPlugin {

	public PluginDescriptionFile info = null;
	public CommandHandler Commands;

	public static void main(String[] args) {
		//Do Nothing
	}

	@Override
	public void onEnable() {
		info = getDescription();
		Commands = new CommandHandler(getDataFolder().getPath() + "/batch.properties");
	}

	@Override
	public void onDisable() {
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("batch")) {
			if (args.length >= 1) {
				if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("batch.reload")) {
					getServer().getPluginManager().disablePlugin(this);
					getServer().getPluginManager().enablePlugin(this);
					sender.sendMessage(info.getName() + " Reloaded!");
				} else if (args[0].equalsIgnoreCase("list") && sender.hasPermission("batch.list")) {
					Commands.listCommandsAndSubCommands(sender);
				} else {
					String alias = args[0];
					if (Commands.commandExists(alias, sender)) {
						sender.sendMessage("Started the command: " + alias);
						Commands.execute(alias, sender, args);
					} else {
						sender.sendMessage("This command does not exist or can only be executed as a player!");
					}
				}
			} else {
				sender.sendMessage("Not enough arguments!");
			}
		}
		return true;
	}
}
