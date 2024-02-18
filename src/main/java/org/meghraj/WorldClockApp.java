package org.meghraj;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.File;
import java.time.ZoneId;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import java.time.LocalTime;
import javafx.scene.control.ChoiceBox;
import java.util.Arrays;
import java.util.List;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.util.Timer;
import java.util.TimerTask;

public class WorldClockApp extends Application {

    private Text[] clocks = new Text[4];
    private ChoiceBox<String>[] countryDropdowns = new ChoiceBox[4];
    private Label alarmLabel = new Label("");
    private Clip[] alarmClips = new Clip[1]; // Array to store alarm clips

    @Override
    public void start(Stage primaryStage) {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        gridPane.setVgap(10);
        gridPane.setHgap(10);

        List<String> europeanCountries = Arrays.asList(
                "United Kingdom", "France", "Germany", "Italy", "Spain",
                "Netherlands", "Belgium", "Switzerland", "Portugal", "Sweden"
        );

        for (int i = 0; i < 4; i++) {
            clocks[i] = createClock();
            gridPane.add(clocks[i], 0, i);

            countryDropdowns[i] = createCountryDropdown();
            countryDropdowns[i].getItems().addAll(europeanCountries);
            gridPane.add(countryDropdowns[i], 1, i);
        }

        Button setAlarmButton = new Button("Set Alarm");
        setAlarmButton.setOnAction(event -> setAlarm());
        gridPane.add(setAlarmButton, 0, 4, 2, 1);

        // Add Stop Alarm button and action
        Button stopAlarmButton = new Button("Stop Alarm");
        stopAlarmButton.setOnAction(event -> stopAlarmSound());
        gridPane.add(stopAlarmButton, 0, 5, 2, 1);

        gridPane.add(alarmLabel, 0, 6, 2, 1);


        Scene scene = new Scene(gridPane, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("World Clock");
        primaryStage.show();

        // Update clocks every second
        Timer clockTimer = new Timer();
        clockTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateClocks();
            }
        }, 0, 1000);
    }

    private Text createClock() {
        Text clock = new Text();
        clock.setFont(Font.font(20));
        return clock;
    }

    private ChoiceBox<String> createCountryDropdown() {
        ChoiceBox<String> dropdown = new ChoiceBox<>();
        dropdown.setValue("United Kingdom");
        return dropdown;
    }

    private void updateClocks() {
        for (int i = 0; i < 4; i++) {
            String country = countryDropdowns[i].getValue();
            if (country != null && !country.isEmpty()) {
                ZoneId zoneId = getZoneIdForCountry(country);
                LocalTime time = LocalTime.now(zoneId);
                clocks[i].setText(getFormattedTime(time));
            }
        }
    }

    private ZoneId getZoneIdForCountry(String country) {
        switch (country) {
            case "United Kingdom":
                return ZoneId.of("Europe/London");
            case "France":
                return ZoneId.of("Europe/Paris");
            case "Germany":
                return ZoneId.of("Europe/Berlin");
            case "Italy":
                return ZoneId.of("Europe/Rome");
            case "Spain":
                return ZoneId.of("Europe/Madrid");
            case "Netherlands":
                return ZoneId.of("Europe/Amsterdam");
            case "Belgium":
                return ZoneId.of("Europe/Brussels");
            case "Switzerland":
                return ZoneId.of("Europe/Zurich");
            case "Portugal":
                return ZoneId.of("Europe/Lisbon");
            case "Sweden":
                return ZoneId.of("Europe/Stockholm");
            default:
                return ZoneId.systemDefault();
        }
    }

    private String getFormattedTime(LocalTime time) {
        return time.toString().substring(0, 8); // Display only hours, minutes, and seconds
    }

    private void setAlarm() {
        ChoiceDialog<String> countryDialog = new ChoiceDialog<>("United Kingdom");
        countryDialog.setTitle("Set Alarm");
        countryDialog.setHeaderText("Select a country for the alarm:");
        countryDialog.setContentText("Country:");
        countryDialog.getItems().addAll(Arrays.asList("United Kingdom", "France", "Germany", "Italy", "Spain",
                "Netherlands", "Belgium", "Switzerland", "Portugal", "Sweden"));

        TextInputDialog timeDialog = new TextInputDialog("00:00");
        timeDialog.setTitle("Set Alarm");
        timeDialog.setHeaderText("Enter alarm time (HH:mm):");
        timeDialog.setContentText("Time:");

        countryDialog.showAndWait().ifPresent(country -> {
            timeDialog.showAndWait().ifPresent(timeStr -> {
                try {
                    LocalTime alarmTime = LocalTime.parse(timeStr);
                    ZoneId zoneId = getZoneIdForCountry(country);
                    long delayMillis = LocalTime.now(zoneId).until(alarmTime, java.time.temporal.ChronoUnit.MILLIS);
                    Timer alarmTimer = new Timer();
                    alarmTimer.schedule(new AlarmTask(country, alarmTime), delayMillis);
                    alarmLabel.setText("Alarm set for " + alarmTime + " in " + country);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private void stopAlarmSound() {
        // Stop the alarm sound by interrupting the clip threads
        for (Clip clip : alarmClips) {
         //   if (clip != null && clip.isRunning()) {
                clip.stop();
                clip.close();
         //   }
        }
        alarmLabel.setText("Alarm stopped");
    }

    private class AlarmTask extends TimerTask {
        private final String country;
        private final LocalTime alarmTime;

        public AlarmTask(String country, LocalTime alarmTime) {
            this.country = country;
            this.alarmTime = alarmTime;
        }

        @Override
        public void run() {
            // Perform alarm action, such as playing a sound
            System.out.println("Alarm for " + country + " at " + alarmTime + ": Time's up!");

            try {
                // Load the sound file
                String soundFile = "src/main/resources/alarm.wav"; // Replace this with the path to your .wav file
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(soundFile));

                // Get the clip
                Clip clip = AudioSystem.getClip();

                // Open the audio input stream
                clip.open(audioInputStream);

                // Start playing the sound
                clip.start();

                // Store the clip reference in the array
                int index = Arrays.asList(clocks).indexOf(country);
                alarmClips[0] = clip;


                // Wait for the user to stop the alarm
                while (clip.isRunning()) {
                    // Sleep for a short time to avoid consuming too much CPU
                    Thread.sleep(1000);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
