package at.ac.tuwien.translator.repository;

import at.ac.tuwien.translator.domain.Language;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Language entity.
 */
@SuppressWarnings("unused")
public interface LanguageRepository extends JpaRepository<Language,Long> {

    @Query("select language from Language language where language.user.id = ?1")
    List<Language> findByUser(Long userId);

    List<Language> findByProjects_id(Long projectId);

    @Query("select count(p) from Project p join p.languages l where l.id = ?1")
    int findNumOfLanguageUsagesInProjects(Long languageId);

    @Query("select count(t) from Translation t join t.language l where l.id = ?1")
    int findNumOfLanguageUsagesInTranslations(Long languageId);

    Language findByName(String languageName);
}
