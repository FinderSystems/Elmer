package pl.finder.elmer.serialization;

import static com.google.common.base.Preconditions.checkState;
import static org.assertj.core.util.Strings.isNullOrEmpty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE, staticName = "create")
final class XmlMessageSerializer implements MessageSerializer {

    @Override
    public Message serialize(final Object message) {
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            final JAXBContext context = JAXBContext.newInstance(message.getClass());
            final Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
            marshaller.marshal(message, output);
            return Message.builder()
                    .body(output.toByteArray())
                    .type(message.getClass().getCanonicalName())
                    .contentType(MessageContentType.XML.value())
                    .build();
        } catch (final JAXBException e) {
            throw new IllegalStateException("Could not serialize message to XML", e);
        } catch (final IOException e) {
            throw new IllegalStateException("Error while writting message to XML", e);
        }
    }

    @Override
    public <T> T deserialize(final Message message) {
        final String type = message.type();
        checkState(!isNullOrEmpty(type), "Unable to determinate messageType");
        final Class<?> messageType = MessageTypes.byName(type);
        return doDeserialize(message, messageType);
    }

    @Override
    public <T> T deserialize(final Message message, final Class<T> messageType) {
        return doDeserialize(message, messageType);
    }

    private static <T> T doDeserialize(final Message message, final Class<?> messageType) {
        try (final InputStream input = message.openStream()) {
            final JAXBContext context = JAXBContext.newInstance(messageType);
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            final Source source = new StreamSource(input);
            @SuppressWarnings("unchecked")
            final T result = (T) unmarshaller.unmarshal(source, messageType)
            .getValue();
            return result;
        } catch (final JAXBException e) {
            throw new IllegalStateException("Could not deserialize message from XML", e);
        } catch (final IOException e) {
            throw new IllegalStateException("Could not read message from XML", e);
        }
    }

}
