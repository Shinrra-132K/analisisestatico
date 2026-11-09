
package co.edu.unbosque.springfirstapp.service;

import co.edu.unbosque.springfirstapp.dto.UserDTO;
import co.edu.unbosque.springfirstapp.model.User;
import co.edu.unbosque.springfirstapp.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Servicio que proporciona operaciones CRUD y funcionalidades adicionales para
 * usuarios. Implementa la interfaz CRUDOperation para operaciones básicas de
 * creación, lectura, actualización y eliminación.
 */
@Service
public class UserService implements CRUDOperation<UserDTO> {

	/** Repositorio para acceder a los datos de usuarios en la base de datos. */
	@Autowired
	private UserRepository userRepo;

	/** Mapeador para convertir entre entidades User y DTOs UserDTO. */
	@Autowired
	private ModelMapper modelMapper;

	/** Codificador para encriptar contraseñas de usuarios. */
	@Autowired
	private PasswordEncoder passwordEncoder;

	/** Constructor por defecto. */
	public UserService() {
	}

	@Override
	public long count() {
		return userRepo.count();
	}

	@Override
	public boolean exist(Long id) {
		return userRepo.existsById(id);
	}

	@Override
	public int create(UserDTO data) {
		User entity = modelMapper.map(data, User.class);
		if (findUsernameAlreadyTaken(entity)) {
			return 1;
		} else {
			// Encriptar la contraseña antes de guardar
			entity.setPassword(passwordEncoder.encode(entity.getPassword()));

			// Establecer rol si se proporciona, de lo contrario se usará el rol
			// predeterminado (USER)
			if (data.getRole() != null) {
				entity.setRole(data.getRole());
			}

			userRepo.save(entity);
			return 0;
		}
	}

	@Override
	public List<UserDTO> getAll() {
		List<User> entityList = userRepo.findAll();
		List<UserDTO> dtoList = new ArrayList<>();

		entityList.forEach((entity) -> {
			UserDTO dto = modelMapper.map(entity, UserDTO.class);
			dtoList.add(dto);
		});

		return dtoList;
	}

	@Override
	public int deleteById(Long id) {
		Optional<User> found = userRepo.findById(id);
		if (found.isPresent()) {
			userRepo.delete(found.get());
			return 0;
		} else {
			return 1;
		}
	}

	public int deleteByUsername(String username) {
		Optional<User> found = userRepo.findByUsername(username);
		if (found.isPresent()) {
			userRepo.delete(found.get());
			return 0;
		} else {
			return 1;
		}
	}

	/**
	 * Actualiza un usuario existente por su ID. Codifica la contraseña antes de
	 * guardarla.
	 */
	@Override
	public int updateById(Long id, UserDTO newData) {
		Optional<User> found = userRepo.findById(id);
		Optional<User> newFound = userRepo.findByUsername(newData.getUsername());

		if (found.isPresent() && !newFound.isPresent()) {
			User temp = found.get();

			temp.setUsername(newData.getUsername());

			// Encriptar la contraseña antes de guardar
			temp.setPassword(passwordEncoder.encode(newData.getPassword()));

			// Actualizar edad
			temp.setEdad(newData.getEdad());

			// Actualizar fecha de nacimiento
			temp.setFechaNacimiento(newData.getFechaNacimiento());

			// Actualizar rol si se proporciona
			if (newData.getRole() != null) {
				temp.setRole(newData.getRole());
			}

			userRepo.save(temp);
			return 0;
		}

		if (found.isPresent() && newFound.isPresent()) {
			return 1;
		}

		if (!found.isPresent()) {
			return 2;
		} else {
			return 3;
		}
	}

	public UserDTO getById(Long id) {
		Optional<User> found = userRepo.findById(id);
		if (found.isPresent()) {
			return modelMapper.map(found.get(), UserDTO.class);
		} else {
			return null;
		}
	}

	public boolean findUsernameAlreadyTaken(User newUser) {
		Optional<User> found = userRepo.findByUsername(newUser.getUsername());
		if (found.isPresent()) {
			return true;
		} else {
			return false;
		}
	}

	public boolean findUsernameAlreadyTaken(String username) {
		Optional<User> found = userRepo.findByUsername(username);
		return found.isPresent();
	}

	public int validateCredentials(String username, String password) {
		Optional<User> userOpt = userRepo.findByUsername(username);

		if (userOpt.isPresent()) {
			User user = userOpt.get();
			if (passwordEncoder.matches(password, user.getPassword())) {
				return 0;
			}
		}

		return 1;
	}
}
