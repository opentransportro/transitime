package org.transitclock.core.dataCache.ehcache.serializers;

import java.nio.ByteBuffer;

import org.ehcache.spi.serialization.Serializer;
import org.ehcache.spi.serialization.SerializerException;
import org.transitclock.core.dataCache.StopEvents;
import org.transitclock.ipc.data.IpcArrivalDeparture;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInputStream;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;

public class StopEventsKyroSerializer implements Serializer<StopEvents> {

	  private static final Kryo kryo = new Kryo();

	  public StopEventsKyroSerializer(ClassLoader loader) {
	    //no-op
		kryo.register(IpcArrivalDeparture.class);
		
		FieldSerializer<IpcArrivalDeparture> serializer = new FieldSerializer<IpcArrivalDeparture>(kryo, IpcArrivalDeparture.class);
		serializer.setCopyTransient(false);		
		kryo.register(IpcArrivalDeparture.class, serializer);		 
	  }

	  @Override
	  public ByteBuffer serialize(final StopEvents object) throws SerializerException {
	    Output output = new Output(4096,-1);
	    kryo.writeObject(output, object);
	    return ByteBuffer.wrap(output.getBuffer());
	  }

	  @Override
	  public StopEvents read(final ByteBuffer binary) throws ClassNotFoundException, SerializerException {
	    Input input =  new Input(new ByteBufferInputStream(binary)) ;
	    return kryo.readObject(input, StopEvents.class);
	  }

	  @Override
	  public boolean equals(final StopEvents object, final ByteBuffer binary) throws ClassNotFoundException, SerializerException {
	    return object.equals(read(binary));
	  }

	}
