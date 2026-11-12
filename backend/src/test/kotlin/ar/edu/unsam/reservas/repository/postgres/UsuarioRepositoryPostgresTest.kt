package ar.edu.unsam.reservas.repository.postgres

import ar.edu.unsam.reservas.domain.BusinessException
import ar.edu.unsam.reservas.domain.Claustro
import ar.edu.unsam.reservas.domain.Rol
import ar.edu.unsam.reservas.domain.Usuario
import ar.edu.unsam.reservas.repository.UsuarioRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

// Corre contra el PostgreSQL de docker-compose. Cada test se deshace al terminar.
@SpringBootTest
@Transactional
@DisplayName("Dado el repositorio de usuarios en PostgreSQL")
class UsuarioRepositoryPostgresTest(@Autowired val usuarios: UsuarioRepository) {

    private fun ana(mail: String = "ana.ruiz@unsam.edu.ar") = Usuario().apply {
        this.mail = mail
        nombre = "Ana"
        apellido = "Ruiz"
        claustro = Claustro.DOCENTE
    }

    @Test
    fun `busca un usuario por su mail`() {
        usuarios.findByMail(DANIELA)!!.nombre shouldBe "Daniela"
        usuarios.findByMail("nadie@unsam.edu.ar") shouldBe null
    }

    @Test
    fun `al dar de alta la base genera el id y pone el rol por defecto`() {
        val creado = usuarios.create(ana())

        val leido = usuarios.findById(creado.id)!!
        leido.mail shouldBe "ana.ruiz@unsam.edu.ar"
        leido.rol shouldBe Rol.USUARIO
        leido.activo shouldBe true
    }

    @Test
    fun `la base rechaza un mail que no es de la universidad aunque nadie lo haya validado antes`() {
        val error = shouldThrow<BusinessException> { usuarios.create(ana("ana@gmail.com")) }

        error.message shouldBe "Hay que usar el mail institucional de la universidad"
    }
}
