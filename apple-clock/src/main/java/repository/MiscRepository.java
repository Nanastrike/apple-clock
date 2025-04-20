package repository;

import model.Misc;

import java.util.List;

public interface MiscRepository  {
    Misc save(Misc misc);
    List<Misc> findAll();
    Misc findById(long id);
}
