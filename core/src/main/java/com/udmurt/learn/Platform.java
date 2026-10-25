package com.udmurt.learn;

/**
 * Мостик к возможностям платформы, которых нет в libGDX.
 * На Android умеет проверять и устанавливать Tailscale; на ПК — заглушка.
 */
public interface Platform {

    boolean isAndroid();

    /** Установлен ли на устройстве Tailscale. */
    boolean hasTailscale();

    /** Открывает магазин приложений для установки Tailscale. */
    void installTailscale();

    /** Запускает уже установленный Tailscale. */
    void openTailscale();

    Platform NONE = new Platform() {
        @Override
        public boolean isAndroid() {
            return false;
        }

        @Override
        public boolean hasTailscale() {
            return false;
        }

        @Override
        public void installTailscale() {
        }

        @Override
        public void openTailscale() {
        }
    };
}
