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
	private ApplicationService applicationService;

	public MyConfigurable() {
		this.applicationService = ApplicationService.getInstance();
		this.state = applicationService.getState();
	}

	@Override
	@Nls
	public String getDisplayName() {
		return "Maven Helper";
	}

	@Override
	@Nullable
	public String getHelpTopic() {
		return null;
	}

	@Override
	@Nullable
	public JComponent createComponent() {
		form = new ApplicationSettingsForm(state);
		return form.getRootComponent();
	}


	@Override
	public boolean isModified() {
		return form != null && form.isSettingsModified(state);
	}

	@Override
	public void apply() throws ConfigurationException {
		applicationService.unRegisterActions();
		state = form.getSettings().clone();
		applicationService.loadState(state);
		applicationService.registerActions();
	}

	@Override
	public void reset() {
		if (form != null) {
			form.importFrom(state);
		}
	}

	@Override
	public void disposeUIResources() {
		form = null;
	}
}
