package com.blacklabelops.crow.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cronutils.utils.StringUtils;

public class VersionAccessor {
		
	public static Logger LOG = LoggerFactory.getLogger(VersionAccessor.class);
	
	public VersionAccessor() {
		super();
	}
	
	public String getVersion() {
		String version = "undefined";
		InputStream metaInf = VersionAccessor.class.getResourceAsStream("/META-INF/MANIFEST.MF");
		if (metaInf != null) {
			Manifest manifest;
			try {
				manifest = new Manifest(metaInf);
			} catch (IOException e) {
				String errorMessage = "Cannot read Manifest for Version String!";
				LOG.error(errorMessage, e);
				throw new RuntimeException(errorMessage, e);
			}
			if (manifest != null) {
				String manifestVersion = (String) manifest.getMainAttributes().get(Attributes.Name.IMPLEMENTATION_VERSION);
				if (!StringUtils.isEmpty(manifestVersion)) {
					version = manifestVersion;
				}
			}
		}
	    return version;
	}
}
