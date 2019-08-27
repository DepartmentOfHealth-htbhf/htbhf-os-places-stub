package uk.gov.dhsc.htbhf;

import com.github.tomakehurst.wiremock.common.ClasspathFileSource;
import com.github.tomakehurst.wiremock.common.FileSource;

/*
 * Without this class Wiremock tries to find the mappings directory under /mappings and the classloader will not find this
 * directory because of the leading slash. This class removes the leading slash and as a consequence the classloader
 * will find the mappings directory.
 */
public class ClasspathFileSourceWithoutLeadingSlash extends ClasspathFileSource {

    ClasspathFileSourceWithoutLeadingSlash() {
        super("");
    }

    @Override
    public FileSource child(String subDirectoryName) {
        return new ClasspathFileSource(subDirectoryName);
    }
}
