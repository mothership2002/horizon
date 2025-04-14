package horizon.web.provider;

import horizon.core.constant.Scheme;
import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class RendezvousProviderLoader {

    public static <I extends RawInput, O extends RawOutput> Map<Scheme, HorizonRendezvousProvider<I, O>> loadProviders() {
        ServiceLoader<HorizonRendezvousProvider<I, O>> loader = ServiceLoader.load(HorizonRendezvousProvider.class);
        Map<Scheme, HorizonRendezvousProvider<I, O>> result = new HashMap<>();

        for (HorizonRendezvousProvider<I, O> provider : loader) {
            Scheme scheme = inferSchemeFromProvider(provider);
            result.put(scheme, provider);
        }

        return result;
    }

    private static <I extends RawInput, O extends RawOutput> Scheme inferSchemeFromProvider(HorizonRendezvousProvider<I, O> provider) {
        return provider.getScheme();
    }

}
