package goofs;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class GoofsProperties {

	protected Properties props;

	public static final GoofsProperties INSTANCE = new GoofsProperties();

	protected GoofsProperties() {

		setProps(new Properties());

		try {
			getProps().load(
					getClass().getClassLoader().getResourceAsStream(
							"goofs-default.properties"));

		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			getProps().load(
					new FileInputStream(new java.io.File(System
							.getProperty("user.home"), ".goofs.properties")));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected Properties getProps() {
		return props;
	}

	protected void setProps(Properties props) {
		this.props = props;
	}

	public String getProperty(String key) {
		return props.getProperty(key);
	}

}
