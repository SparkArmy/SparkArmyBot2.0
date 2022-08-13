package de.SparkArmy.timedOperations;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.controller.GuildConfigType;
import de.SparkArmy.utils.FileHandler;
import de.SparkArmy.utils.MainUtil;
import de.SparkArmy.utils.punishmentUtils.PunishmentUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimedOperations {
    private static final JDA jda = MainUtil.jda;
    private static final ConfigController controller = MainUtil.controller;

    public static void removeOldTemporaryPunishments() {
        try {

            File directory = FileHandler.getDirectoryInUserDirectory("botstuff/timed-punishments");
            if (directory == null) {
                MainUtil.logger.info("Can't create/ get a directory for timed-punishments");
                return;
            }

            File file = FileHandler.getFileInDirectory(directory, "entrys.json");
            if (!file.exists()) return;


            String fileContent = FileHandler.getFileContent(file);
            if (fileContent == null || fileContent.isEmpty()) return;
            JSONObject entrys = new JSONObject(fileContent);
            List<String> keyList = new ArrayList<>();
            if (entrys.isEmpty()) return;
            entrys.keySet().forEach(key -> {
                JSONObject entry = entrys.getJSONObject(key);
                DateTimeFormatter formatter = PunishmentUtils.punishmentFormatter;
                String time = entry.getString("expirationTime");
                String timeNow = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System.currentTimeMillis()));
                boolean timeReached = LocalDateTime.parse(timeNow, formatter).isAfter(LocalDateTime.parse(time, formatter));
                if (timeReached) {
                    Guild guild = jda.getGuildById(entry.getString("guild"));
                    if (guild == null) {
                        keyList.add(key);
                        return;
                    }
                    JSONObject config = controller.getSpecificGuildConfig(guild, GuildConfigType.MAIN);
                    if (config.isNull("punishments")) {
                        keyList.add(key);
                        return;
                    }
                    JSONObject punishments = config.getJSONObject("punishments");
                    String type = entry.getString("type");
                    User user = jda.retrieveUserById(entry.getString("user")).complete();
                    if (user == null) {
                        keyList.add(key);
                        return;
                    }
                    if (type.equals("warn") || type.equals("mute")) {
                        String roleId = punishments.getJSONObject(type).getString("role-id");
                        if (roleId.equals("Empty")) {
                            keyList.add(key);
                            return;
                        } else if (guild.getRoleById(roleId) == null) {
                            keyList.add(key);
                            return;
                        }

                        //noinspection ConstantConditions
                        if (guild.getMember(user).getRoles().isEmpty() || !guild.getMember(user).getRoles().contains(guild.getRoleById(roleId))) {
                            keyList.add(key);
                            return;
                        }

                        //noinspection ConstantConditions
                        guild.removeRoleFromMember(user, guild.getRoleById(roleId)).reason("Automatic role remove").queue();
                        keyList.add(key);
                        return;
                    }
                    if (type.equals("ban")) {
                        guild.unban(user).reason("Automatic unban").queue();
                        keyList.add(key);
                        return;
                    }

                    keyList.add(key);
                }
            });
            if (keyList.isEmpty()) return;
            keyList.forEach(entrys::remove);
            FileHandler.writeValuesInFile(file,entrys);
        }catch (Exception e){
        e.printStackTrace();
        }
    }
}
