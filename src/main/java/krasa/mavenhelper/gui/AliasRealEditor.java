package krasa.mavenhelper.gui;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.CheckBoxList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import krasa.mavenhelper.action.MavenProjectInfo;
import krasa.mavenhelper.model.ApplicationSettings;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import javax.swing.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Class Name is AliasRealEditor
 *
 * @author LiJun
 * Created on 2024/6/25 17:28
 */
public class AliasRealEditor extends DialogWrapper {

    private static final String PROPERTIES_KEY = "MavenHelper.AliasRealEditor.";

    private static final Set<String> ALIAS = Set.of(ApplicationSettings.MODULES, ApplicationSettings.VERSION);

    // $xxx$ pattern
    private static final Pattern REGEX = Pattern.compile("\\$(.*?)\\$");

    private CheckBoxList<MavenProject> modules = null;
    private JBTextField versionField;
    private List<String> versionSuggests;
    private final Map<String, JBTextField> editorFields;
    private final MavenProject mavenProject;

    public AliasRealEditor(String command, @NotNull MavenProjectInfo mavenProjectInfo, @NotNull MavenProjectsManager manager) {
        super(false);
        MavenProject mavenProject = Validate.notNull(mavenProjectInfo.getCurrentOrRootMavenProject(), "maven project not found");
        this.mavenProject = mavenProject;
        if (command.contains(ApplicationSettings.MODULES)) {
            Collection<MavenProject> projects = manager.findInheritors(mavenProject);
            this.modules = new CheckBoxList<>();
            this.modules.setItems(new ArrayList<>(projects), MavenProject::getDisplayName);
            List<String> lastSelected = PropertiesComponent.getInstance().getList(PROPERTIES_KEY + mavenProject.getDisplayName());
            // history
            if (CollectionUtils.isNotEmpty(lastSelected)) {
                Set<String> lastSelectedSet = new HashSet<>(lastSelected);
                for (MavenProject project : projects) {
                    if (lastSelectedSet.contains(project.getDisplayName())) {
                        this.modules.setItemSelected(project, true);
                    }
                }
            } else {
                projects.forEach(p -> modules.setItemSelected(p, true));
            }
        }
        if (command.contains(ApplicationSettings.VERSION)) {
            String version = StringUtils.defaultString(mavenProject.getMavenId().getVersion());
            this.versionField = new JBTextField(version);
            this.versionSuggests = new ArrayList<>();
            this.versionSuggests.add(version);
            if (StringUtils.endsWith(version, "-SNAPSHOT")) {
                this.versionSuggests.add(StringUtils.removeEnd(version, "-SNAPSHOT"));
                this.versionSuggests.add(version.substring(0, version.length() - 9) + "-preon-SNAPSHOT");
                this.versionSuggests.add(version.substring(0, version.length() - 9) + "-PREON-SNAPSHOT");
            } else {
                this.versionSuggests.add(version + "-SNAPSHOT");
            }
        }
        editorFields = new LinkedHashMap<>();
        Matcher matcher = REGEX.matcher(command);
        while (matcher.find()) {
            String field = matcher.group(0);
            if (!editorFields.containsKey(field)) {
                editorFields.put(field, new JBTextField());
            }
        }
        setTitle("Edit Custom Info...");
        init();
    }

    public static boolean needEditor(String command) {
        if (StringUtils.isBlank(command)) {
            return false;
        }
        if (ALIAS.stream().anyMatch(command::contains)) {
            return true;
        }
        Matcher matcher = REGEX.matcher(command);
        if (matcher.find()) {
            return true;
        }
        return false;
    }

    public static String alias(String command, @NotNull MavenProjectInfo mavenProjectInfo, MavenProjectsManager manager) {
        if (null == manager || !needEditor(command)) {
            return command;
        }
        AliasRealEditor editor = new AliasRealEditor(command, mavenProjectInfo, manager);
        if (editor.showAndGet()) {
            if (command.contains(ApplicationSettings.MODULES)) {
                String project = editor.getSelectModules().stream().map(MavenProject::getDisplayName).collect(Collectors.joining(","));
                command = command.replace(ApplicationSettings.MODULES, project);
            }
            if (command.contains(ApplicationSettings.VERSION)) {
                String version = editor.versionField.getText();
                command = command.replace(ApplicationSettings.VERSION, version);
            }
        }
        return command;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        FormBuilder builder = FormBuilder.createFormBuilder();
        if (null != modules) {
            // builder.addComponent(new JLabel("$modules$"));
            builder.addLabeledComponentFillVertically("$modules$", modules);
        }
        if (null != versionField) {
            versionField.setPreferredSize(JBUI.size(200, 30));
            builder.addComponent(versionField);
            BorderLayoutPanel panel = BorderLayoutPanel.getInstance(0, 0).center(versionField).right(suggestButton());
            builder.addLabeledComponent("$version$", panel);
        }
        if (MapUtils.isNotEmpty(editorFields)) {
            editorFields.forEach(builder::addLabeledComponent);
        }
        builder.addVerticalGap(-1);
        JBScrollPane pane = new JBScrollPane(builder.getPanel());
        pane.setMaximumSize(JBUI.size(500, 800));
        pane.setMinimumSize(JBUI.size(50, 100));
        return pane;
    }

    @NotNull
    public List<MavenProject> getSelectModules() {
        int count;
        if (null == modules || (count = modules.getItemsCount()) == 0) {
            return List.of();
        }
        return IntStream.range(0, count).mapToObj(modules::getItemAt).filter(Objects::nonNull)
                .filter(modules::isItemSelected).toList();
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{getOKAction()};
    }

    @Override
    protected void dispose() {
        super.dispose();
        if (versionField != null) {
            List<String> lastSelected = getSelectModules().stream().map(MavenProject::getDisplayName)
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(lastSelected)) {
                PropertiesComponent.getInstance().setList(PROPERTIES_KEY + mavenProject.getDisplayName(), lastSelected);
            } else {
                PropertiesComponent.getInstance().unsetValue(PROPERTIES_KEY + mavenProject.getDisplayName());
            }
        }

    }

    private JButton suggestButton() {
        JButton button = new JButton();
        button.setText("Maybe");
        button.addActionListener(event -> JBPopupFactory.getInstance().createPopupChooserBuilder(versionSuggests)
                .setItemChosenCallback(versionField::setText)
                .createPopup()
                .showUnderneathOf(button));
        return button;
    }
}
