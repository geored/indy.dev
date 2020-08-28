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
package org.commonjava.indy.ftest.core.content;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.commonjava.indy.client.core.IndyClientException;
import org.commonjava.indy.ftest.core.AbstractContentManagementTest;
import org.commonjava.indy.model.core.Group;
import org.commonjava.indy.model.core.RemoteRepository;
import org.commonjava.test.http.expect.ExpectationServer;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Rule;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.commonjava.indy.model.core.StoreType.group;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * <b>GIVEN:</b>
 * <ul>
 *     <li>A group contains two constituents</li>
 *     <li>Both constituents has same artifacts with metadata</li>
 *     <li>First access to the group metadata with both constituents enabled got correct result</li>
 * </ul>
 *
 * <br/>
 * <b>WHEN:</b>
 * <ul>
 *     <li>One constituent is disabled</li>
 * </ul>
 *
 * <br/>
 * <b>THEN:</b>
 * <ul>
 *     <li>Group metadata should be updated correctly except the disabled one</li>
 * </ul>
 */
public class GroupMetadataRemergeWhenConstituentDisabledTest
        extends AbstractContentManagementTest
{

    @Rule
    public ExpectationServer server = new ExpectationServer();

    @Test
    public void run()
            throws Exception
    {
        final String repo1 = "repo1";
        final String repo2 = "repo2";
        final String path = "org/foo/bar/maven-metadata.xml";

        /* @formatter:off */
        final String repo1Content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
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

        /* @formatter:off */
        final String repo2Content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<metadata>\n" +
            "  <groupId>org.foo</groupId>\n" +
            "  <artifactId>bar</artifactId>\n" +
            "  <versioning>\n" +
            "    <latest>2.0</latest>\n" +
            "    <release>2.0</release>\n" +
            "    <versions>\n" +
            "      <version>2.0</version>\n" +
            "    </versions>\n" +
            "    <lastUpdated>20160722164334</lastUpdated>\n" +
            "  </versioning>\n" +
            "</metadata>\n";
        /* @formatter:on */

        /* @formatter:off */
        final String mergedContent ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<metadata>\n" +
                "  <groupId>org.foo</groupId>\n" +
                "  <artifactId>bar</artifactId>\n" +
                "  <versioning>\n" +
                "    <latest>2.0</latest>\n" +
                "    <release>2.0</release>\n" +
                "    <versions>\n" +
                "      <version>1.0</version>\n" +
                "      <version>2.0</version>\n" +
                "    </versions>\n" +
                "    <lastUpdated>20160722164334</lastUpdated>\n" +
                "  </versioning>\n" +
                "</metadata>";
        /* @formatter:on */

        server.expect( server.formatUrl( repo1, path ), 200, repo1Content );
        server.expect( server.formatUrl( repo2, path ), 200, repo2Content );

        RemoteRepository remote1 = new RemoteRepository( repo1, server.formatUrl( repo1 ) );

        remote1 = client.stores()
                        .create( remote1, "adding remote1", RemoteRepository.class );

        RemoteRepository remote2 = new RemoteRepository( repo2, server.formatUrl( repo2 ) );

        remote2 = client.stores()
                        .create( remote2, "adding remote2", RemoteRepository.class );

        Group g = new Group( "test", remote1.getKey(), remote2.getKey() );
        g = client.stores()
                  .create( g, "adding group", Group.class );

        System.out.printf( "\n\nGroup constituents are:\n  %s\n\n", StringUtils.join( g.getConstituents(), "\n  " ) );

        InputStream stream = client.content()
                                         .get( group, g.getName(), path );

        assertThat( stream, notNullValue() );

        String metadata = IOUtils.toString( stream );
        assertContent( metadata, mergedContent );
        stream.close();

        remote2.setDisabled( true );
        client.stores().update( remote2, "updating remote2" );

        Thread.sleep( 1000 );

        stream = client.content().get( group, g.getName(), path );

        assertThat( stream, notNullValue() );

        metadata = IOUtils.toString( stream );
        assertContent( metadata, repo1Content );
        stream.close();
    }
    private void assertContent( String actual, String expectedXml )
            throws IndyClientException, IOException
    {

        logger.debug( "Comparing downloaded XML:\n\n{}\n\nTo expected XML:\n\n{}\n\n", actual, expectedXml );

        try
        {
            XMLUnit.setIgnoreWhitespace( true );
            XMLUnit.setIgnoreDiffBetweenTextAndCDATA( true );
            XMLUnit.setIgnoreAttributeOrder( true );
            XMLUnit.setIgnoreComments( true );

            assertXMLEqual( actual, expectedXml );
        }
        catch ( SAXException e )
        {
            e.printStackTrace();
            fail( "Downloaded XML not equal to expected XML" );
        }
    }

    @Override
    protected boolean createStandardTestStructures()
    {
        return false;
    }
}