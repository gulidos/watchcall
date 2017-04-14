package ru.in.watcher.web;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ru.in.watcher.conf.Settings;



@Controller
@RequestMapping("/")
public class HomeController {
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

	@Autowired
	private Settings settings;

	@RequestMapping(value = "/",method = GET)
	public String home(Model model) {
//		model.addAttribute("settings", settings.getParameters());
		return "redirect:/settings";	
		}
	

	
	@RequestMapping(value = "/reload", method = RequestMethod.GET)
	public String reloadSetings(Model model,
			RedirectAttributes redirectAttrs,
			@RequestParam(value="phase", required=true) String phase) {

		if (phase.equals("reload")) {
			try {
				settings.loadSettings();
				redirectAttrs.addFlashAttribute("msgInfo", "Настройки обновлены");
			} catch (Throwable e) {
				redirectAttrs.addFlashAttribute("msgWarn", e.getMessage());
				logger.error(e.getMessage(), e);
			}
		} 
		return "redirect:/settings";
	}

}
