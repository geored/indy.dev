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
package org.commonjava.indy.bind.jaxrs;

import org.commonjava.indy.metrics.RequestContextHelper;
import org.commonjava.indy.metrics.TrafficClassifier;
import org.commonjava.indy.sli.metrics.GoldenSignalsFunctionMetrics;
import org.commonjava.indy.sli.metrics.GoldenSignalsMetricSet;
import org.commonjava.maven.galley.spi.io.SpecialPathManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.join;
import static org.commonjava.indy.IndyContentConstants.NANOS_PER_MILLISECOND;
import static org.commonjava.indy.metrics.RequestContextHelper.REQUEST_LATENCY_MILLIS;
import static org.commonjava.indy.metrics.RequestContextHelper.REQUEST_LATENCY_NS;

@ApplicationScoped
public class GoldenSignalsFilter
    implements Filter
{
    @Inject
    private GoldenSignalsMetricSet metricSet;

    @Inject
    private TrafficClassifier classifier;

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    // For Unit-testing
    GoldenSignalsFilter( final GoldenSignalsMetricSet metricSet, TrafficClassifier classifier )
    {
        this.metricSet = metricSet;
        this.classifier = classifier;
    }

    GoldenSignalsFilter() {}

    @Override
    public void init( final FilterConfig filterConfig )
    {
    }

    @Override
    public void doFilter( final ServletRequest servletRequest, final ServletResponse servletResponse,
                          final FilterChain filterChain )
            throws IOException, ServletException
    {
        logger.trace( "START: {}", getClass().getSimpleName() );

        long start = System.nanoTime();

        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;
        try
        {
            Set<String> functions = new HashSet<>( classifier.classifyFunctions( req.getPathInfo(), req.getMethod() ) );
            functions.forEach( function -> metricSet.function( function ).ifPresent(
                    GoldenSignalsFunctionMetrics::started ) );
        }
        catch ( Exception e )
        {
            logger.error( "Failed to classify / measure load for: " + req.getPathInfo(), e );
        }

        try
        {
            filterChain.doFilter( req, resp );
        }
        catch ( IOException | ServletException | RuntimeException e )
        {
            new HashSet<>( classifier.classifyFunctions( req.getPathInfo(), req.getMethod() ) ).forEach(
                    function -> metricSet.function( function ).ifPresent( GoldenSignalsFunctionMetrics::error ) );
            throw e;
        }
        finally
        {
            // In some cases, we cannot calculate latency without capturing actual data transfer time. When this happens,
            // we can capture the actual data transfer time and subtract it from the total execution time to get
            // latency.
            long end = RequestContextHelper.getRequestEndNanos() - RequestContextHelper.getRawIoWriteNanos();

            RequestContextHelper.setContext( REQUEST_LATENCY_NS, String.valueOf( end - start ) );
            RequestContextHelper.setContext( REQUEST_LATENCY_MILLIS, (end-start) / NANOS_PER_MILLISECOND  );

            Set<String> functions = new HashSet<>( classifier.classifyFunctions( req.getPathInfo(), req.getMethod() ) );
            boolean error = resp.getStatus() > 499;

            functions.forEach( function -> metricSet.function( function ).ifPresent( ms -> {
                ms.latency( end-start ).call();
                if ( error )
                {
                    ms.error();
                }
            } ) );

            logger.trace( "END: {}", getClass().getSimpleName() );
        }
    }

    @Override
    public void destroy()
    {
    }

}
