package sample;

public class BitcoinMarketInfo {
    class Time {
        String updated;
        String updatedISO;
    }

    Time time;
    String disclaimer;

    class BPI {
        class Currency {
            String code;
            String symbol;
            String rate;
            String description;
            float rate_float;

            public float getRate_float() {
                return rate_float;
            }
        }

        Currency USD;

        public float getRate_float() {
            return USD.getRate_float();
        }
    }

    BPI bpi;

    public float getRate_float() {
        return bpi.getRate_float();
    }
}