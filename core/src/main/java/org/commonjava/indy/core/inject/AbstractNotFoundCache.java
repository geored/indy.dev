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
package org.commonjava.indy.core.inject;

import org.commonjava.indy.conf.IndyConfiguration;
import org.commonjava.indy.model.core.StoreKey;
import org.commonjava.indy.model.galley.RepositoryLocation;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Created by ruhan on 12/1/17.
 */
public abstract class AbstractNotFoundCache implements NotFoundCache
{

    public Map<Location, Set<String>> getAllMissing( int pageIndex, int pageSize )
    {
        return Collections.emptyMap();
    }

    public Set<String> getMissing( Location location, int pageIndex, int pageSize )
    {
        return Collections.emptySet();
    }

    abstract public long getSize( StoreKey storeKey );

    abstract public long getSize();

    protected int getTimeoutInSeconds( ConcreteResource resource )
    {
        int timeoutInSeconds = getIndyConfiguration().getNotFoundCacheTimeoutSeconds();
        Location loc = resource.getLocation();
        Integer to = loc.getAttribute( RepositoryLocation.ATTR_NFC_TIMEOUT_SECONDS, Integer.class );
        if ( to != null && to > 0 )
        {
            timeoutInSeconds = to;
        }
        return timeoutInSeconds;
    }

    protected abstract IndyConfiguration getIndyConfiguration();

}
