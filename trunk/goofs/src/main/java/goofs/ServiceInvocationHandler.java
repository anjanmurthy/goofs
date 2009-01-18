package goofs;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.google.gdata.client.GoogleService;

public class ServiceInvocationHandler implements InvocationHandler {

	public GoofsService target;

	public ServiceInvocationHandler(GoofsService target) {

		setTarget(target);

	}

	protected GoofsService getTarget() {
		return target;
	}

	protected void setTarget(GoofsService target) {
		this.target = target;
	}

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {

		try {

			return method.invoke(getTarget(), args);

		} catch (InvocationTargetException e) {

			if (e.getCause() instanceof GoogleService.SessionExpiredException) {

				// let's try to re-establish a connection

				getTarget().getRealService().setUserCredentials(
						System.getProperty("username"),
						System.getProperty("password"));

				// try one more time

				return method.invoke(getTarget(), args);
			}

			throw e;

		}

	}

}
