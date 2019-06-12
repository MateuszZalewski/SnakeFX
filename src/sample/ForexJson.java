package sample;

public class ForexJson {
    Rates rates;

    private class Rates {
        ExchangeRates EURUSD;

        private class ExchangeRates {
            double rate;
            long timestamp;

            public double getRate() {
                return rate;
            }
        }

        public double getRate() {
            return EURUSD.getRate();
        }
    }

    public double getRate() {
        return rates.getRate();
    }

    int code;
}
