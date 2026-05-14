package me.luiz.penseira.Controller;

import me.luiz.penseira.contracts.ILogger;
import me.luiz.penseira.service.LogService;

import java.util.logging.Logger;

public class BotController {
    private final ILogger logger;

    public BotController() {
        this.logger = new LogService();
    }
}
