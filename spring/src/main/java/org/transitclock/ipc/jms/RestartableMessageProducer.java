/* (C)2023 */
package org.transitclock.ipc.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.naming.NamingException;
import java.io.Serializable;

/**
 * Sometimes when sending a message it will turn out that the session and/or the producer will have
 * been closed. This class allows the session and producer to automatically be restarted if
 * necessary. This means that writing messages will not fail when running on a laptop that is woken
 * up after being asleep.
 *
 * @author SkiBu Smith
 */
public class RestartableMessageProducer {
    private JMSWrapper jmsWrapper;
    private Destination destination;
    MessageProducer messageProducer;

    private static final Logger logger = LoggerFactory.getLogger(RestartableMessageProducer.class);

    /********************** Member Functions **************************/

    /**
     * Constructor
     *
     * @param jmsWrapper
     * @param destination
     * @throws JMSException
     */
    public RestartableMessageProducer(JMSWrapper jmsWrapper, Destination destination) throws JMSException {
        this.jmsWrapper = jmsWrapper;
        this.destination = destination;

        createProducer();
    }

    /**
     * Sets messageProducer. Part of initialization but can also be called if the connection has
     * closed and the producer needs to be recreated.
     *
     * @throws JMSException
     */
    private void createProducer() throws JMSException {
        messageProducer = jmsWrapper.getSession().createProducer(destination);
    }

    /**
     * For sending a message to a Topic or a Queue. The message can be any serializable object.
     *
     * @param object
     * @throws JMSException
     */
    public void sendObjectMessage(Serializable object) throws JMSException {
        ObjectMessage objectMessage;
        try {
            objectMessage = jmsWrapper.getSession().createObjectMessage(object);
        } catch (JMSException e) {
            // Sessions can sometimes get closed, like when running the
            // service on a laptop and then closing it. For this situation
            // try opening session again and then try to create the object
            // message again.
            logger.error("Trying to open session/connection again because when calling "
                    + "createObjectMessage() got JMSException "
                    + e.getMessage());
            try {
                jmsWrapper.initiateConnection();
            } catch (NamingException e1) {
                logger.error("Got NamingException exeption when trying to re-initiate the connection", e);
                // Throw the initial JMSException
                throw e;
            }
            // Now try creating the message again. If it fails this time
            // simply let the error be thrown since wasn't successful
            // handling it here.
            objectMessage = jmsWrapper.getSession().createObjectMessage(object);
        }

        try {
            messageProducer.send(objectMessage);
        } catch (Exception e) {
            // Producers are sometimes closed so try opening it up again
            // and then send the message again.
            logger.error("Trying to send create producer and then send "
                    + "message again because got JMSException "
                    + e.getMessage());
            createProducer();
            // Now try sending the message again
            messageProducer.send(objectMessage);
        }
    }

    /**
     * For sending a message to a Topic or a Queue. The message can be any serializable object.
     *
     * @param object
     * @throws JMSException
     */
    public void sendTextMessage(String message) throws JMSException {
        TextMessage textMessage;
        try {
            textMessage = jmsWrapper.getSession().createTextMessage(message);
        } catch (JMSException e) {
            // Sessions can sometimes get closed, like when running the
            // service on a laptop and then closing it. For this situation
            // try opening session again and then try to create the object
            // message again.
            logger.error("Trying to open session again because got " + "JMSException " + e.getMessage());
            try {
                jmsWrapper.initiateConnection();
            } catch (NamingException e1) {
                logger.error("Got NamingException exeption when trying to " + "re-initiate the connection", e);
                // Throw the initial JMSException
                throw e;
            }
            // Now try creating the message again
            textMessage = jmsWrapper.getSession().createTextMessage(message);
        }

        try {
            messageProducer.send(textMessage);
        } catch (Exception e) {
            // Producers are sometimes closed so try opening it up again
            // and then send the message again.
            logger.error("Trying to send message again because got " + "JMSException " + e.getMessage());
            createProducer();
            // Now try sending the message again
            messageProducer.send(textMessage);
        }
    }
}
