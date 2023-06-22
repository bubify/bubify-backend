package com.uu.au.controllers;

import com.uu.au.JpaConfig;
import com.uu.au.models.Achievement;
import com.uu.au.models.User;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

@Controller
public class BackupController {
    private final ConcurrentLinkedDeque<Pair<User, Achievement>> achievementUnlockedBackupBacklog = new ConcurrentLinkedDeque<>();
    private BufferedWriter achievementsUnlockedBackup = null;

    @Value("${server.backup.dir}")
    private String achievementBackupFileFileName;
    private File achievementsUnlockedBackupFile = null;
    private int achievementsWrittenToFile = 0;

    public void createDirectoriesIfNeeded() {
        var dir = new File(achievementBackupFileFileName);

        /// Should be a directory
        if (dir.isFile()) {
            dir = new File(dir.getParentFile(), "backups@" + LocalDateTime.now());
            dir.mkdir();
            achievementBackupFileFileName = dir.getAbsolutePath();
        } else {
            if (!dir.exists()) {
                dir.mkdir();
            }
        }
    }

    private boolean firstRun = true;

    @Scheduled(fixedRate = 10 * 1000)
    public void backupAchievementsUnlocked() throws IOException {
        if (firstRun) {
            createDirectoriesIfNeeded();
            firstRun = false;
        }

        var now = LocalDateTime.now().toString();

        if (achievementsUnlockedBackupFile == null) {
            achievementsUnlockedBackupFile = new File(achievementBackupFileFileName + "unlocked." + now + ".csv");
            achievementsUnlockedBackup = new BufferedWriter(new FileWriter(achievementsUnlockedBackupFile));
        }

        while (!achievementUnlockedBackupBacklog.isEmpty()) {
            var e = achievementUnlockedBackupBacklog.remove();
            var user = e.getLeft();
            var achievement = e.getRight();

            achievementsUnlockedBackup.write(user.getUserName());
            achievementsUnlockedBackup.write(";");
            achievementsUnlockedBackup.write(achievement.getCode());
            achievementsUnlockedBackup.write(";");
            achievementsUnlockedBackup.write(now);
            achievementsUnlockedBackup.write("\n");

            achievementsWrittenToFile += 1;

            System.err.println("Backup results for " + user.getUserName());
        }

        achievementsUnlockedBackup.flush();

        /// Rotate logs to avoid file corruption
        if (achievementsWrittenToFile > 128) {
            achievementsUnlockedBackup.close();
            achievementsUnlockedBackupFile = null;
            achievementsWrittenToFile = 0;
        }
    }

    public void backupUnlockedAchievement(User user, Achievement achievement) {
        achievementUnlockedBackupBacklog.offer(Pair.of(user, achievement));
    }
}
