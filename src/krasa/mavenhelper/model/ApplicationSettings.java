package krasa.mavenhelper.model;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.psi.PsiFile;
import com.intellij.util.xmlb.annotations.Transient;
import com.rits.cloning.Cloner;
import krasa.mavenhelper.ApplicationComponent;
import krasa.mavenhelper.action.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ApplicationSettings extends DomainObject implements Cloneable {
	private static final Collection<String> BASIC_PHASES = MavenConstants.BASIC_PHASES;
	public static final String CURRENT_CLASS_MACRO = "<<<CURRENT_CLASS>>>";
	public static final String CURRENT_CLASS_WITH_METHOD_MACRO = "<<<CURRENT_CLASS_WITH_TEST_METHOD>>>";

	int version = 1;
	private boolean useIgnoredPoms = false;
	private Goals goals = new Goals();
	private Goals pluginAwareGoals = new Goals();
	private Aliases aliases = new Aliases();
	private boolean enableDelete = true;

	public ApplicationSettings() {
		Goals pluginAwareGoals = new Goals();
		pluginAwareGoals.add("jetty:run");
		pluginAwareGoals.add("tomcat:run");
		pluginAwareGoals.add("tomcat5:run");
		pluginAwareGoals.add("tomcat6:run");
		pluginAwareGoals.add("tomcat7:run");
		setPluginAwareGoals(pluginAwareGoals);

		Goals goals = new Goals();
		for (String basicPhase : BASIC_PHASES) {
			goals.add(new Goal(basicPhase));
		}
		goals.add(new Goal("clean install"));
		setGoals(goals);
		setUseIgnoredPoms(false);

		addDefaultAliases(this.aliases);
	}

	@NotNull
	public static ApplicationSettings get() {
		ApplicationComponent instance = ApplicationComponent.getInstance();
		return instance.getState();
	}

	public Aliases getAliases() {
		return aliases;
	}

	public void setAliases(Aliases aliases) {
		this.aliases = aliases;
	}

	public boolean isUseIgnoredPoms() {
		return useIgnoredPoms;
	}

	public void setUseIgnoredPoms(boolean useIgnoredPoms) {
		this.useIgnoredPoms = useIgnoredPoms;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Goals getPluginAwareGoals() {
		return pluginAwareGoals;
	}

	public void setPluginAwareGoals(Goals pluginAwareGoals) {
		this.pluginAwareGoals = pluginAwareGoals;
	}

	public Goals getGoals() {
		return goals;
	}

	public void setGoals(Goals goals) {
		this.goals = goals;
	}

	public static void resetDefaultAliases(List<Alias> myAliases) {
		Aliases aliases = addDefaultAliases(new Aliases());
		Map<String, String> stringStringMap = aliases.asMap();
		myAliases.removeIf(next -> stringStringMap.containsKey(next.getFrom()));
		myAliases.addAll(aliases.getAliases());
	}

	@Transient
	public static Aliases addDefaultAliases(Aliases aliases) {
		aliases.add(new Alias("$class$", CURRENT_CLASS_MACRO));
		aliases.add(new Alias("$classWithMethod$", CURRENT_CLASS_WITH_METHOD_MACRO));
		return aliases;
	}

	@Transient
	public List<Goal> getAllGoals() {
		List<Goal> allGoals = new ArrayList<Goal>(goals.size() + pluginAwareGoals.size());
		allGoals.addAll(goals.getGoals());
		allGoals.addAll(pluginAwareGoals.getGoals());
		return allGoals;
	}

	@Transient
	public List<String> getAllGoalsAsString() {
		List<String> strings = new ArrayList<String>();
		List<Goal> allGoals = getAllGoals();
		for (Goal allGoal : allGoals) {
			strings.add(allGoal.getCommandLine());
		}
		return strings;
	}

	@Override
	public ApplicationSettings clone() {
		Cloner cloner = new Cloner();
		cloner.nullInsteadOfClone();
		return cloner.deepClone(this);
	}

	@Transient
	public String[] getAllGoalsAsStringArray() {
		return toArray(getAllGoalsAsString());
	}

	private String[] toArray(List<String> goalsAsStrings) {
		return goalsAsStrings.toArray(new String[goalsAsStrings.size()]);
	}

	public boolean removeGoal(Goal goal) {
		boolean remove = goals.remove(goal);
		if (!remove) {
			remove = pluginAwareGoals.remove(goal);
		}
		return remove;
	}

	public String applyAliases(String commandLine, PsiFile psiFile, ConfigurationContext fromContext) {
		String s = aliases.applyAliases(commandLine);

		if (s.contains(CURRENT_CLASS_MACRO)) {
			s = s.replace(CURRENT_CLASS_MACRO, Utils.getQualifiedName(psiFile));
		}
		if (s.contains(CURRENT_CLASS_WITH_METHOD_MACRO)) {
			String to = Utils.getTestArgument(psiFile, fromContext);
			if (Utils.NOT_RESOLVED.equals(to)) {
				to = Utils.getQualifiedName(psiFile);
			}
			s = s.replace(CURRENT_CLASS_WITH_METHOD_MACRO, to);
		}
		return s;
	}

	public boolean isEnableDelete() {
		return enableDelete;
	}

	public void setEnableDelete(final boolean enableDelete) {
		this.enableDelete = enableDelete;
	}
}
