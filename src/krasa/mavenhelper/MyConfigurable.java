package krasa.mavenhelper;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import krasa.mavenhelper.gui.ApplicationSettingsForm;
import krasa.mavenhelper.model.ApplicationSettings;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class MyConfigurable implements Configurable {
	private ApplicationSettingsForm form;
	public ApplicationSettings state;
	private ApplicationComponent applicationComponent;

	public MyConfigurable(ApplicationComponent 	applicationComponent) {
		this.applicationComponent = applicationComponent;
		this.state = applicationComponent.getState();
	}

	@Nls
	public String getDisplayName() {
		return "Maven Helper";
	}

	@Nullable
	public String getHelpTopic() {
		return null;
	}

	@Nullable
	public JComponent createComponent() {
		form = new ApplicationSettingsForm(state);
		return form.getRootComponent();
	}

	
	public boolean isModified() {
		return form != null && form.isSettingsModified(state);
	}

	public void apply() throws ConfigurationException {
		applicationComponent.unRegisterActions();
		state = form.getSettings().clone();
		applicationComponent.loadState(state);
		applicationComponent.registerActions();
	}

	public void reset() {
		if (form != null) {
			form.importFrom(state);
		}
	}

	public void disposeUIResources() {
		form = null;
	}
}
