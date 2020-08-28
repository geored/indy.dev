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
package org.commonjava.indy.pkg.maven.content;

import org.commonjava.indy.client.core.IndyClientException;
import org.commonjava.indy.ftest.core.AbstractContentManagementTest;
import org.commonjava.indy.model.core.Group;
import org.commonjava.indy.model.core.HostedRepository;
import org.commonjava.indy.util.LocationUtils;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;

import static org.commonjava.maven.galley.io.SpecialPathConstants.PKG_TYPE_MAVEN;
import static org.jgroups.util.Util.assertFalse;
import static org.jgroups.util.Util.assertTrue;

/**
 * Check that the group's metadata is deleted when user make a force deletion request.
 * <br/>
 * GIVEN:
 * <ul>
 *     <li>HostedRepositories A</li>
 *     <li>Group G1 with members hosted:A</li>
 *     <li>Group G2 with members G1</li>
 *     <li>HostedRepository A contains a valid Path P metadata file</li>
 *     <li>Path P has been requested from Group G1 and G2</li>
 * </ul>
 * <br/>
 * WHEN:
 * <ul>
 *     <li>Path P is deleted from Group G1</li>
 * </ul>
 * <br/>
 * THEN:
 * <ul>
 *     <li>Cached metadata in Group G1 and G2 are deleted.</li>
 *     <li>Cached metadata in Group G1 are regenerated after request it again.</li>
 * </ul>
 */
public class GroupMetadataFileDeletionTest
                extends AbstractContentManagementTest
{
    private static final String METADATA_PATH = "org/foo/bar/maven-metadata.xml";

    /* @formatter:off */
    private static final String METADATA_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<metadata>\n" +
        "  <groupId>org.foo</groupId>\n" +
        "  <artifactId>bar</artifactId>\n" +
        "  <versioning>\n" +
        "    <latest>1.0</latest>\n" +
        "    <release>1.0</release>\n" +
        "    <versions>\n" +
        "      <version>1.0</version>\n" +
        "    </versions>\n" +
        "    <lastUpdated>20150722164334</lastUpdated>\n" +
        "  </versioning>\n" +
        "</metadata>\n";
    /* @formatter:on */

    private Group g1, g2;

    private HostedRepository a;

    @Before
    public void setupRepos() throws Exception
    {
        String message = "test setup";

        a = client.stores().create( new HostedRepository( PKG_TYPE_MAVEN, "a" ), message, HostedRepository.class );

        g1 = client.stores().create( new Group( "g1", a.getKey() ), message, Group.class );
        g2 = client.stores().create( new Group( "g2", g1.getKey() ), message, Group.class );

        deployContent( a, METADATA_CONTENT, METADATA_PATH );

        assertContent( g1, METADATA_PATH, METADATA_CONTENT );
        assertContent( g2, METADATA_PATH, METADATA_CONTENT );
    }

    @Test
    public void run() throws Exception
    {
        File f1 = getPhysicalStorageFile( LocationUtils.toLocation( g1 ), METADATA_PATH );
        File f2 = getPhysicalStorageFile( LocationUtils.toLocation( g2 ), METADATA_PATH );

        // Files exist
        assertTrue( f1.exists() );
        assertTrue( f2.exists() );

        // Delete cache file from G1, which will delete parent G2's cache too
        client.content().deleteCache( g1.getKey(), METADATA_PATH );

        sleepAndRunFileGC( 1000 );

        // Verify files were deleted
        ConcreteResource r1 = new ConcreteResource( LocationUtils.toLocation( g1 ), METADATA_PATH );
        ConcreteResource r2 = new ConcreteResource( LocationUtils.toLocation( g2 ), METADATA_PATH );
        assertFalse( cacheProvider.exists( r1 ) );
        assertFalse( cacheProvider.exists( r2 ) );

        // Re-generate
        assertContent( g1, METADATA_PATH, METADATA_CONTENT );
    }

    private void deployContent( HostedRepository repo, String content, String path ) throws IndyClientException
    {
        client.content().store( repo.getKey(), path, new ByteArrayInputStream( content.getBytes() ) );
    }

    @Override
    protected boolean createStandardTestStructures()
    {
        return false;
    }
}
