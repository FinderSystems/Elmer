package pl.finder.elmer.serialization;

import java.lang.reflect.Type;

import pl.finder.elmer.SerializationException;


public interface MessageSerializer {

	<TMessage> byte[] serialize(TMessage message)
			throws SerializationException;

	<TMessage> TMessage deserialize(byte[] message, Type type)
			throws SerializationException;
}
