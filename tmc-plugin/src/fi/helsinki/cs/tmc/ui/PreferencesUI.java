package fi.helsinki.cs.tmc.ui;


import fi.helsinki.cs.tmc.core.domain.Course;

import java.util.List;
import java.util.Locale;

public interface PreferencesUI {

    String getProjectDir();
    
    List<Course> getAvailableCourses();
            
    boolean getCheckForUpdatesInTheBackground();
    
    boolean getCheckForUnopenedExercisesAtStartup();
    
    String getSelectedCourseName();
    
    boolean getSpywareEnabled();
    
    boolean getResolveProjectDependenciesEnabled();
    
    void setResolveProjectDependenciesEnabled(boolean value);
    
    boolean getSendDiagnosticsEnabled();

    void setSendDiagnosticsEnabled(boolean value);

    Locale getErrorMsgLocale();
    
    void setProjectDir(String projectDir);
                
    void setCheckForUpdatesInTheBackground(boolean shouldCheck);
    
    void setCheckForUnopenedExercisesAtStartup(boolean shouldCheck);
    
    void setSpywareEnabled(boolean enabled);
    
    void setErrorMsgLocale(Locale locale);
}
