package pl.finder.elmer;

import java.lang.reflect.Type;


public interface MessageSerializer {

	<TMessage> byte[] serialize(TMessage message)
			throws SerializationException;

	<TMessage> TMessage deserialize(byte[] message, Type type)
			throws SerializationException;
}
