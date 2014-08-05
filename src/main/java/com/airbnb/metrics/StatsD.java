package com.airbnb.metrics;

/**
 * Copyright (C) 2014-2015 Alexis Midon alexis.midon@airbnb.com
 * Copyright (C) 2012-2013 Sean Laurent
 * Copyright (C) 2013 metrics-statsd contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */


import org.apache.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * A client to a StatsD server.
 */
public class StatsD implements Closeable {

    private final Logger logger = Logger.getLogger(getClass());

    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private final DatagramSocketFactory socketFactory;

    private InetSocketAddress address;
    private DatagramSocket socket;
    private int failures;

    /**
     * Creates a new client which connects to the given address using the default {@link DatagramSocketFactory}.
     *
     * @param host the hostname of the StatsD server.
     * @param port the port of the StatsD server. This is typically 8125.
     */
    public StatsD(final String host, final int port) {
        this(new InetSocketAddress(host, port), new DatagramSocketFactory());
    }

    /**
     * Creates a new client which connects to the given address and socket factory.
     *
     * @param address       the address of the Carbon server
     * @param socketFactory the socket factory
     */
    public StatsD(final InetSocketAddress address, final DatagramSocketFactory socketFactory) {
        this.address = address;
        this.socketFactory = socketFactory;
    }

    /**
     * Resolves the address hostname if present.
     * <p/>
     * Creates a datagram socket through the factory.
     *
     * @throws IllegalStateException if the client is already connected
     * @throws java.io.IOException           if there is an error connecting
     */
    public void connect() throws IOException {
        if (socket != null) {
            throw new IllegalStateException("Already connected");
        }

        if (address.getHostName() != null) {
            this.address = new InetSocketAddress(address.getHostName(), address.getPort());
        }

        this.socket = socketFactory.createSocket();
    }

    /**
     * Sends the given measurement to the server. Logs exceptions.
     *
     * @param name  the name of the metric
     * @param value the value of the metric
     */
    public void send(final String name, final String value) {
        try {
            String formatted = String.format("%s:%s|g", sanitize(name), sanitize(value));
            byte[] bytes = formatted.getBytes(UTF_8);
            socket.send(socketFactory.createPacket(bytes, bytes.length, address));
            failures = 0;
        } catch (IOException e) {
            failures++;

            if (failures == 1) {
                logger.warn("unable to send packet to statsd at '"+address.getHostName()+":"+address.getPort()+"'");
            } else {
                if(logger.isDebugEnabled())
                    logger.debug("unable to send packet to statsd at '"+address.getHostName()+":"+address.getPort()+"'");
            }
        }
    }

    /**
     * Returns the number of failed writes to the server.
     *
     * @return the number of failed writes to the server
     */
    public int getFailures() {
        return failures;
    }

    @Override
    public void close() throws IOException {
        if (socket != null) {
            socket.close();
        }
        this.socket = null;
    }

    private String sanitize(final String s) {
        return WHITESPACE.matcher(s).replaceAll("-");
    }
}

