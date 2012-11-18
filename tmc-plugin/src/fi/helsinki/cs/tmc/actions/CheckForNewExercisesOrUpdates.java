package fi.helsinki.cs.tmc.actions;

import fi.helsinki.cs.tmc.data.Course;
import fi.helsinki.cs.tmc.data.CourseListUtils;
import fi.helsinki.cs.tmc.model.CourseDb;
import fi.helsinki.cs.tmc.model.LocalExerciseStatus;
import fi.helsinki.cs.tmc.model.ObsoleteClientException;
import fi.helsinki.cs.tmc.model.ServerAccess;
import fi.helsinki.cs.tmc.model.TmcSettings;
import fi.helsinki.cs.tmc.ui.DownloadOrUpdateExercisesDialog;
import fi.helsinki.cs.tmc.ui.ConvenientDialogDisplayer;
import fi.helsinki.cs.tmc.utilities.BgTask;
import fi.helsinki.cs.tmc.utilities.BgTaskListener;
import fi.helsinki.cs.tmc.utilities.Inflector;
import fi.helsinki.cs.tmc.utilities.TmcStringUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import org.apache.commons.lang3.StringUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "TMC",
id = "fi.helsinki.cs.tmc.actions.CheckForNewExercisesOrUpdates")
@ActionRegistration(displayName = "#CTL_CheckForNewExercisesOrUpdates")
@ActionReferences({
    @ActionReference(path = "Menu/TM&C", position = -50)
})
@Messages("CTL_CheckForNewExercisesOrUpdates=&Download/update exercises")
public class CheckForNewExercisesOrUpdates extends AbstractAction {

    public static void startTimer() {
        int interval = 20*60*1000; // 20 minutes
        javax.swing.Timer timer = new javax.swing.Timer(interval, new CheckForNewExercisesOrUpdates(true, true));
        timer.setRepeats(true);
        timer.start();
    }
    
    
    private CourseDb courseDb;
    private ServerAccess serverAccess;
    private NotificationDisplayer notifier;
    private ConvenientDialogDisplayer dialogs;
    private boolean beQuiet;
    private boolean backgroundCheck;

    public CheckForNewExercisesOrUpdates() {
        this(false, false);
    }
    
    public CheckForNewExercisesOrUpdates(boolean beQuiet, boolean backgroundCheck) {
        this.courseDb = CourseDb.getInstance();
        this.serverAccess = new ServerAccess();
        this.notifier = NotificationDisplayer.getDefault();
        this.dialogs = ConvenientDialogDisplayer.getDefault();
        this.beQuiet = beQuiet;
        this.backgroundCheck = backgroundCheck;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        run();
    }
    
    public void run() {
        final Course currentCourse = courseDb.getCurrentCourse();
        
        if (backgroundCheck && !TmcSettings.getDefault().isCheckingForUpdatesInTheBackground()) {
            return;
        }
        
        if (currentCourse == null) {
            if (!beQuiet) {
                dialogs.displayMessage("Please select a course in TMC -> Settings.");
            }
            return;
        }
        
        BgTask.start("Checking for new exercises", serverAccess.getDownloadingCourseListTask(), new BgTaskListener<List<Course>>() {
            @Override
            public void bgTaskReady(List<Course> receivedCourseList) {
                Course receivedCourse = CourseListUtils.getCourseByName(receivedCourseList, currentCourse.getName());
                if (receivedCourse != null) {
                    courseDb.setAvailableCourses(receivedCourseList);

                    final LocalExerciseStatus status = LocalExerciseStatus.get(receivedCourse.getExercises());
                    if (status.thereIsSomethingToDownload()) {
                        if (beQuiet) {
                            displayNotification(status, new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    DownloadOrUpdateExercisesDialog.display(status.unlockable, status.downloadable, status.updateable);
                                }
                            });
                        } else {
                            DownloadOrUpdateExercisesDialog.display(status.unlockable, status.downloadable, status.updateable);
                        }
                    } else if (!beQuiet) {
                        dialogs.displayMessage("No new exercises or updates to download.");
                    }
                }
            }

            @Override
            public void bgTaskCancelled() {
            }

            @Override
            public void bgTaskFailed(Throwable ex) {
                if (!beQuiet || ex instanceof ObsoleteClientException) {
                    dialogs.displayError("Failed to check for new exercises.\n" + ServerErrorHelper.getServerExceptionMsg(ex));
                }
            }
        });
    }

    private void displayNotification(LocalExerciseStatus status, ActionListener action) {
        ArrayList<String> items = new ArrayList<String>();
        ArrayList<String> actions = new ArrayList<String>();
        
        if (!status.unlockable.isEmpty()) {
            items.add(Inflector.pluralize(status.unlockable.size(), "an unlockable exercise"));
            actions.add("unlock");
        }
        if (!status.downloadable.isEmpty()) {
            items.add(Inflector.pluralize(status.downloadable.size(), "a new exercise"));
            actions.add("download");
        }
        if (!status.updateable.isEmpty()) {
            items.add(Inflector.pluralize(status.updateable.size(), "an update"));
            actions.add("update");
        }
        
        int total =
                status.unlockable.size() +
                status.downloadable.size() +
                status.updateable.size();
        
        String msg = TmcStringUtils.joinCommaAnd(items);
        msg += " " + Inflector.pluralize(total, "is") + " available.";
        msg = StringUtils.capitalize(msg);
        
        String prompt = "Click here to " + TmcStringUtils.joinCommaAnd(actions) + ".";
        
        notifier.notify(msg, getNotificationIcon(), prompt, action);
    }

    private Icon getNotificationIcon() {
        return ImageUtilities.loadImageIcon("fi/helsinki/cs/tmc/smile.gif", false);
    }
}
