package com.example.ruvo_app.core.di

import android.content.Context
import com.example.ruvo_app.data.local.UserPreferencesManager
import com.example.ruvo_app.data.repository.*
import com.example.ruvo_app.domain.repository.*
import com.example.ruvo_app.domain.usecase.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideUserPreferencesManager(
        @ApplicationContext context: Context
    ): UserPreferencesManager = UserPreferencesManager(context)

    @Provides
    @Singleton
    fun provideUserRepository(firestore: FirebaseFirestore): UserRepository = UserRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        userRepository: UserRepository
    ): AuthRepository = AuthRepositoryImpl(auth, userRepository)

    @Provides
    @Singleton
    fun provideServiceRepository(
        firestore: FirebaseFirestore
    ): ServiceRepository = ServiceRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideNotificationRepository(
        firestore: FirebaseFirestore
    ): NotificationRepository = NotificationRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideChatRepository(
        firestore: FirebaseFirestore
    ): ChatRepository = ChatRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideServiceRequestRepository(
        firestore: FirebaseFirestore
    ): ServiceRequestRepository = ServiceRequestRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideCommentRepository(
        firestore: FirebaseFirestore
    ): CommentRepository = CommentRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideReviewRepository(
        firestore: FirebaseFirestore,
        userRepository: UserRepository
    ): ReviewRepository = ReviewRepositoryImpl(firestore, userRepository)

    @Provides
    @Singleton
    fun provideAuthUseCases(
        login: LoginUseCase,
        register: RegisterUseCase,
        signOut: SignOutUseCase,
        getCurrentUser: GetCurrentUserUseCase,
        sendPasswordReset: SendPasswordResetEmailUseCase
    ) = AuthUseCases(
        login = login,
        register = register,
        signOut = signOut,
        getCurrentUser = getCurrentUser,
        sendPasswordReset = sendPasswordReset
    )

    @Provides
    @Singleton
    fun provideLoginUseCase(repository: AuthRepository) = LoginUseCase(repository)

    @Provides
    @Singleton
    fun provideRegisterUseCase(repository: AuthRepository) = RegisterUseCase(repository)

    @Provides
    @Singleton
    fun provideSignOutUseCase(repository: AuthRepository) = SignOutUseCase(repository)

    @Provides
    @Singleton
    fun provideGetCurrentUserUseCase(repository: AuthRepository) = GetCurrentUserUseCase(repository)

    @Provides
    @Singleton
    fun provideResetPasswordUseCase(repository: AuthRepository) = ResetPasswordUseCase(repository)

    @Provides
    @Singleton
    fun provideSendPasswordResetEmailUseCase(repository: AuthRepository) = SendPasswordResetEmailUseCase(repository)

    @Provides
    @Singleton
    fun provideValidateEmailUseCase() = ValidateEmailUseCase()

    @Provides
    @Singleton
    fun provideValidatePasswordUseCase() = ValidatePasswordUseCase()

    @Provides
    @Singleton
    fun provideUpdateUserProfileUseCase(repository: UserRepository) = UpdateUserProfileUseCase(repository)

    @Provides
    @Singleton
    fun provideValidateUsernameUseCase() = ValidateUsernameUseCase()

    @Provides
    @Singleton
    fun provideChangeUserRoleUseCase(repository: UserRepository) = ChangeUserRoleUseCase(repository)

    @Provides
    @Singleton
    fun provideGetServicePostsByAuthorUseCase(repository: ServiceRepository) = GetServicePostsByAuthorUseCase(repository)
}

data class AuthUseCases(
    val login: LoginUseCase,
    val register: RegisterUseCase,
    val signOut: SignOutUseCase,
    val getCurrentUser: GetCurrentUserUseCase,
    val sendPasswordReset: SendPasswordResetEmailUseCase
)
