package horizon.http.provider;

import com.google.auto.service.AutoService;
import horizon.core.constant.Scheme;
import horizon.core.rendezvous.AbstractRendezvous;
import horizon.http.HttpInterpreter;
import horizon.http.HttpNormalizer;
import horizon.http.model.HttpInput;
import horizon.http.model.HttpOutput;
import horizon.web.provider.HorizonRendezvousProvider;
import horizon.web.rendezvous.RendezvousBuilder;

@AutoService(HorizonRendezvousProvider.class)
public class HttpRendezvousProvider implements HorizonRendezvousProvider<HttpInput, HttpOutput> {

    @Override
    public AbstractRendezvous<HttpInput, HttpOutput> build(RendezvousBuilder<HttpInput, HttpOutput> builder) {
        return builder
                .withScheme(Scheme.HTTP)
                .withNormalizer(new HttpNormalizer())
                .withInterpreter(new HttpInterpreter())
                .build();
    }

}