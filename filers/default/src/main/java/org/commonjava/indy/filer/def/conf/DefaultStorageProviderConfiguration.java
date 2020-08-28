/**
 * Copyright (C) 2011-2020 Red Hat, Inc. (https://github.com/Commonjava/indy)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.indy.filer.def.conf;

import org.commonjava.indy.conf.IndyConfigInfo;
import org.commonjava.indy.conf.SystemPropertyProvider;
import org.commonjava.propulsor.config.annotation.ConfigName;
import org.commonjava.propulsor.config.annotation.SectionName;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.io.InputStream;
import java.util.Properties;

@SectionName( "storage-default" )
@ApplicationScoped
public class DefaultStorageProviderConfiguration
                implements IndyConfigInfo, SystemPropertyProvider
{

    public static final File DEFAULT_BASEDIR = new File( "/var/lib/indy/storage" );

    // NOTE: Providing a default value negates the detection of whether the NFS CacheProvider should be used or not, in DefaultGalleyStorageProvider.
//    public static final File DEFAULT_NFS_BASEDIR = new File("/mnt/nfs/var/lib/indy/storage");

    public static final String STORAGE_DIR = "indy.storage.dir";

    public static final String NFS_STORAGE_DIR = "indy.storage.nfs.dir";

    private File storageBasedir;

    private File nfsStoreBasedir;

    public DefaultStorageProviderConfiguration()
    {
    }

    public DefaultStorageProviderConfiguration( final File storageBasedir )
    {
        this( storageBasedir, null );
    }

    public DefaultStorageProviderConfiguration( final File storageBasedir, final File nfsStoreBasedir )
    {
        this.storageBasedir = storageBasedir;
        this.nfsStoreBasedir = nfsStoreBasedir;
    }

    public File getStorageRootDirectory()
    {
        return storageBasedir == null ? DEFAULT_BASEDIR : storageBasedir;
    }

    @Deprecated
    public File getNFSStorageRootDirectory()
    {
        return nfsStoreBasedir;
    }

    @ConfigName( "storage.dir" )
    @Deprecated
    public void setStorageRootDirectory( final File storageBasedir )
    {
        this.storageBasedir = storageBasedir;
    }

    @ConfigName( "storage.nfs.dir" )
    public void setNFSStorageRootDirectory( final File nfsStorageRootDirectory )
    {
        this.nfsStoreBasedir = nfsStorageRootDirectory;
    }

    @Override
    public String getDefaultConfigFileName()
    {
        return IndyConfigInfo.APPEND_DEFAULTS_TO_MAIN_CONF;
    }

    @Override
    public InputStream getDefaultConfig()
    {
        return Thread.currentThread()
                     .getContextClassLoader()
                     .getResourceAsStream( "default-storage.conf" );
    }

    @Override
    public Properties getSystemPropertyAdditions()
    {
        Properties p = new Properties();
        p.setProperty( STORAGE_DIR, getStorageRootDirectory().getAbsolutePath() );
        p.setProperty( NFS_STORAGE_DIR, getStorageRootDirectory().getAbsolutePath() );
        return p;
    }

    // Path mapped storage config

    private static final String DEFAULT_STORAGE_KEYSPACE = "indystorage";

    private String cassandraKeyspace = DEFAULT_STORAGE_KEYSPACE;

    private int gcBatchSize = 100;

    private int gcGracePeriodInHours = 24;

    private int gcIntervalInMinutes = 30;

    private String fileChecksumAlgorithm = "SHA-256";

    private String deduplicatePattern = "^(generic|npm).+";

    @ConfigName( "storage.cassandra.keyspace" )
    public void setCassandraKeyspace( String keyspace )
    {
        cassandraKeyspace = keyspace;
    }

    public String getCassandraKeyspace()
    {
        return cassandraKeyspace;
    }

    @ConfigName( "storage.gc.batchsize" )
    public void setGcBatchSize( int gcBatchSize )
    {
        this.gcBatchSize = gcBatchSize;
    }

    public int getGcBatchSize()
    {
        return gcBatchSize;
    }

    @ConfigName( "storage.gc.graceperiodinhours" )
    public void setGcGracePeriodInHours( int gcGracePeriodInHours )
    {
        this.gcGracePeriodInHours = gcGracePeriodInHours;
    }

    public int getGcGracePeriodInHours()
    {
        return gcGracePeriodInHours;
    }

    @ConfigName( "storage.gc.intervalinminutes" )
    public void setGcIntervalInMinutes( int gcIntervalInMinutes )
    {
        this.gcIntervalInMinutes = gcIntervalInMinutes;
    }

    public int getGcIntervalInMinutes()
    {
        return gcIntervalInMinutes;
    }

    @ConfigName( "storage.file.checksum.algorithm" )
    public void setFileChecksumAlgorithm( String fileChecksumAlgorithm )
    {
        this.fileChecksumAlgorithm = fileChecksumAlgorithm;
    }

    public String getFileChecksumAlgorithm()
    {
        return fileChecksumAlgorithm;
    }

    @ConfigName( "storage.deduplicate.pattern" )
    public void setDeduplicatePattern( String deduplicatePattern )
    {
        this.deduplicatePattern = deduplicatePattern;
    }

    public String getDeduplicatePattern()
    {
        return deduplicatePattern;
    }
}
