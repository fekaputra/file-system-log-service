package ac.at.tuwien.logparser.view;

import ac.at.tuwien.logparser.services.LogConverterService;
import ac.at.tuwien.logparser.view.transfer.SearchParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class ViewController {

    @Autowired
    private LogConverterService logConverterService;


    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/search", method = RequestMethod.POST, produces = "application/json")
    public String searchPathname(@RequestBody SearchParam param) {
        System.out.println("search history for: " + param.getPathname());
        String graph = logConverterService.getHistoryJson(param.getPathname());
        return graph;
    }
}
