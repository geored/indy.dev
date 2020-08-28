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

import org.commonjava.indy.ftest.core.category.TimingDependent;
import org.commonjava.indy.model.core.RemoteRepository;
import org.commonjava.maven.galley.model.Location;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.commonjava.indy.model.core.StoreType.remote;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * <b>GIVEN:</b>
 * <ul>
 *     <li>{@link RemoteRepository} is set with "connection-timeout" with 1s</li>
 *     <li>{@link RemoteRepository} is set with 2s disable timeout</li>
 *     <li>The remote proxy gives a connection timeout error for repo</li>
 * </ul>
 *
 * <br/>
 * <b>WHEN:</b>
 * <ul>
 *     <li>Request repo for artifact and got timeout error</li>
 * </ul>
 *
 * <br/>
 * <b>THEN:</b>
 * <ul>
 *     <li>The remote repo will be set to disable when got the error</li>
 *     <li>After about 2 seconds it will be re-enabled</li>
 * </ul>
 */
public class RemoteRepoTimeoutReenableWithTimeoutSetTest
        extends AbstractRemoteRepoTimeoutTest
{

    @Category( TimingDependent.class )
    @Test
    public void runTest()
            throws Exception
    {
        super.run();
    }

    @Override
    protected void setRemoteTimeout( RemoteRepository remoteRepo )
    {
        remoteRepo.setMetadata( Location.CONNECTION_TIMEOUT_SECONDS, Integer.toString( 1 ) );
        remoteRepo.setDisableTimeout( 2 );
    }

    @Override
    protected void assertResult( RemoteRepository remoteRepo )
            throws Exception
    {
        assertThat( remoteRepo.isDisabled(), equalTo( true ) );

        Thread.sleep( 2000 );

        RemoteRepository result = client.stores().load( remote, remoteRepo.getName(), RemoteRepository.class );
        assertThat( result.isDisabled(), equalTo( false ) );
    }
}