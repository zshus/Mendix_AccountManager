package jxlser.implementation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jxls.util.JxlsHelper;
import org.jxls.util.Util;

import com.mendix.core.Core;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.systemwideinterfaces.core.meta.IMetaObject;

import system.proxies.FileDocument;

public class JxlserProcessor {

	public static void doProcessTemplate(IContext mxcontext, InputStream templateIs, FileDocument mergedXlsFile, String targetCell) throws Exception {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {

			org.jxls.common.Context jxcontext = new org.jxls.common.Context();
            Map<String, Object> params = JxlserParameterUtil.getNextParameters();
            for (String key : params.keySet()) {
            	Object param = params.get(key);
            	if (param instanceof List) {
            		List<IMendixObject> moList = (List<IMendixObject>)param;
            		List<Object> proxyList = new ArrayList<Object>();
            		for (IMendixObject mo: moList) {
            			proxyList.add(toProxyObject(mxcontext, mo));
            		}
            		jxcontext.putVar(key, proxyList);
            	} else {
            		jxcontext.putVar(key, toProxyObject(mxcontext, (IMendixObject)params.get(key)));
            	}
            }
            if (targetCell != null && !targetCell.equals("")) {
            	JxlsHelper.getInstance().processTemplateAtCell(templateIs, os, jxcontext, targetCell);
            } else {
            	JxlsHelper.getInstance().processTemplate(templateIs, os, jxcontext);
            }
            Core.storeFileDocumentContent(mxcontext, mergedXlsFile.getMendixObject(),
                    new ByteArrayInputStream(os.toByteArray()));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static Object toProxyObject(IContext mxcontext, IMendixObject mo) throws Exception
	{
		if (mo == null) {
			return null;
		}

		IMetaObject meta = mo.getMetaObject();
		if (meta.getSuperObject() != null && meta.getSuperObject().getName().equals("System.Image")) {
			InputStream is = Core.getImage(mxcontext, mo, false);
			return Util.toByteArray(is);
		} else {
			String proxyClassName = meta.getModuleName().toLowerCase() + ".proxies." + meta.getName().replace(meta.getModuleName()+".", "");
		    Class clazz = Class.forName(proxyClassName);
		    Method initializeer = clazz.getDeclaredMethod("initialize", IContext.class, IMendixObject.class);
		    Object proxy = initializeer.invoke(null, mxcontext, mo);
		    return proxy;
		}
	}

}
