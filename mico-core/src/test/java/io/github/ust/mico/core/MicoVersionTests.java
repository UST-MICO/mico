package io.github.ust.mico.core;

import static org.junit.Assert.assertEquals;

import io.github.ust.mico.core.exception.VersionNotSupportedException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.github.ust.mico.core.model.MicoVersion;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MicoVersionTests {

    @Test
    public void createVersionForString() throws VersionNotSupportedException {
        MicoVersion version = MicoVersion.valueOf("1.0.0");
        assertEquals(version.getMajorVersion(), 1);
        assertEquals(version.getMinorVersion(), 0);
        assertEquals(version.getPatchVersion(), 0);
        
        version.setPreReleaseVersion("foo");
        assertEquals(version.getMajorVersion(), 1);
        assertEquals(version.getMinorVersion(), 0);
        assertEquals(version.getPatchVersion(), 0);
        assertEquals(version.getPreReleaseVersion(), "foo");
        
        version.setPreReleaseVersion("0.1.1");
        assertEquals(version.getMajorVersion(), 1);
        assertEquals(version.getMinorVersion(), 0);
        assertEquals(version.getPatchVersion(), 0);
        assertEquals(version.getPreReleaseVersion(), "0.1.1");
        
        version.setPreReleaseVersion("rc.1");
        assertEquals(version.getMajorVersion(), 1);
        assertEquals(version.getMinorVersion(), 0);
        assertEquals(version.getPatchVersion(), 0);
        assertEquals(version.getPreReleaseVersion(), "rc.1");
    }
    
    @Test
    public void createVersionForStringWithPrefix() throws VersionNotSupportedException {
        MicoVersion version = MicoVersion.valueOf("v1.0.0");
        assertEquals(version.getPrefix(), "v");
        assertEquals(version.getMajorVersion(), 1);
        assertEquals(version.getMinorVersion(), 0);
        assertEquals(version.getPatchVersion(), 0);
        
        version.setPreReleaseVersion("foo");
        assertEquals(version.getPrefix(), "v");
        assertEquals(version.getMajorVersion(), 1);
        assertEquals(version.getMinorVersion(), 0);
        assertEquals(version.getPatchVersion(), 0);
        assertEquals(version.getPreReleaseVersion(), "foo");
        
        version.setPreReleaseVersion("0.1.1");
        assertEquals(version.getPrefix(), "v");
        assertEquals(version.getMajorVersion(), 1);
        assertEquals(version.getMinorVersion(), 0);
        assertEquals(version.getPatchVersion(), 0);
        assertEquals(version.getPreReleaseVersion(), "0.1.1");
        
        version.setPreReleaseVersion("rc.1");
        assertEquals(version.getPrefix(), "v");
        assertEquals(version.getMajorVersion(), 1);
        assertEquals(version.getMinorVersion(), 0);
        assertEquals(version.getPatchVersion(), 0);
        assertEquals(version.getPreReleaseVersion(), "rc.1");
    }
    
    @Test
    public void createVersionForValues() {
        MicoVersion version = MicoVersion.forIntegers(22, 4, 17);
        assertEquals(version.getMajorVersion(), 22);
        assertEquals(version.getMinorVersion(), 4);
        assertEquals(version.getPatchVersion(), 17);
        
        version.setPreReleaseVersion("foo");
        assertEquals(version.getMajorVersion(), 22);
        assertEquals(version.getMinorVersion(), 4);
        assertEquals(version.getPatchVersion(), 17);
        assertEquals(version.getPreReleaseVersion(), "foo");
        
        version.setPreReleaseVersion("0.1.1");
        assertEquals(version.getMajorVersion(), 22);
        assertEquals(version.getMinorVersion(), 4);
        assertEquals(version.getPatchVersion(), 17);
        assertEquals(version.getPreReleaseVersion(), "0.1.1");
        
        version.setPreReleaseVersion("rc.1");
        assertEquals(version.getMajorVersion(), 22);
        assertEquals(version.getMinorVersion(), 4);
        assertEquals(version.getPatchVersion(), 17);
        assertEquals(version.getPreReleaseVersion(), "rc.1");
    }
    
    @Test
    public void createVersionForValuesWithPrefix() {
        MicoVersion version = MicoVersion.forIntegersWithPrefix("v", 22, 4, 17);
        assertEquals(version.getPrefix(), "v");
        assertEquals(version.getMajorVersion(), 22);
        assertEquals(version.getMinorVersion(), 4);
        assertEquals(version.getPatchVersion(), 17);
        
        version.setPreReleaseVersion("foo");
        assertEquals(version.getPrefix(), "v");
        assertEquals(version.getMajorVersion(), 22);
        assertEquals(version.getMinorVersion(), 4);
        assertEquals(version.getPatchVersion(), 17);
        assertEquals(version.getPreReleaseVersion(), "foo");
        
        version.setPreReleaseVersion("0.1.1");
        assertEquals(version.getPrefix(), "v");
        assertEquals(version.getMajorVersion(), 22);
        assertEquals(version.getMinorVersion(), 4);
        assertEquals(version.getPatchVersion(), 17);
        assertEquals(version.getPreReleaseVersion(), "0.1.1");
        
        version.setPreReleaseVersion("rc.1");
        assertEquals(version.getPrefix(), "v");
        assertEquals(version.getMajorVersion(), 22);
        assertEquals(version.getMinorVersion(), 4);
        assertEquals(version.getPatchVersion(), 17);
        assertEquals(version.getPreReleaseVersion(), "rc.1");
    }
    
    @Test
    public void incrementTest() {
        MicoVersion version = MicoVersion.forIntegers(1, 0, 0);
        assertEquals(version.getMajorVersion(), 1);
        assertEquals(version.getMinorVersion(), 0);
        assertEquals(version.getPatchVersion(), 0);
        
        version.incrementMajorVersion();
        assertEquals(version.getMajorVersion(), 2);
        assertEquals(version.getMinorVersion(), 0);
        assertEquals(version.getPatchVersion(), 0);
        
        version.incrementMinorVersion();
        assertEquals(version.getMajorVersion(), 2);
        assertEquals(version.getMinorVersion(), 1);
        assertEquals(version.getPatchVersion(), 0);
        
        version.incrementPatchVersion();
        assertEquals(version.getMajorVersion(), 2);
        assertEquals(version.getMinorVersion(), 1);
        assertEquals(version.getPatchVersion(), 1);
        
        version.incrementMajorVersion("1");
        assertEquals(version.getMajorVersion(), 3);
        assertEquals(version.getMinorVersion(), 0);
        assertEquals(version.getPatchVersion(), 0);
        assertEquals(version.getPreReleaseVersion(), "1");
        
        version.incrementMinorVersion("2");
        assertEquals(version.getMajorVersion(), 3);
        assertEquals(version.getMinorVersion(), 1);
        assertEquals(version.getPatchVersion(), 0);
        assertEquals(version.getPreReleaseVersion(), "2");
        
        version.incrementPatchVersion("3");
        assertEquals(version.getMajorVersion(), 3);
        assertEquals(version.getMinorVersion(), 1);
        assertEquals(version.getPatchVersion(), 1);
        assertEquals(version.getPreReleaseVersion(), "3");
        
    }
    
    @Test(expected = NullPointerException.class)
    public void createNullVersion() throws VersionNotSupportedException {
        MicoVersion.valueOf(null);
    }
    
    @Test(expected = VersionNotSupportedException.class)
    public void createEmptyVersion() throws VersionNotSupportedException {
        MicoVersion.valueOf("");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void createNegativeVersionMajor() {
        MicoVersion.forIntegers(-1, 0, 0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void createNegativeVersionMinor() {
        MicoVersion.forIntegers(0, -1, 0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void createNegativeVersionPatch() {
        MicoVersion.forIntegers(0, 0, -1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void createNegativeVersionMajorWithPrefix() {
        MicoVersion.forIntegersWithPrefix("v", -1, 0, 0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void createNegativeVersionMinorWithPrefix() {
        MicoVersion.forIntegersWithPrefix("v", 0, -1, 0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void createNegativeVersionPatchWithPrefix() {
        MicoVersion.forIntegersWithPrefix("v", 0, 0, -1);
    }

}
