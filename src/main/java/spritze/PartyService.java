package spritze;

@Komponente
public class PartyService {
    private MusikService musikService;

    public PartyService(MusikService musikService) {
        this.musikService = musikService;
    }

    public void party() {
        musikService.play();
    }
}
